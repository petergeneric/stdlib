package com.peterphi.servicemanager.service.ssl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.microsoft.azure.management.dns.models.RecordType;
import com.peterphi.servicemanager.service.db.dao.impl.LetsEncryptCertificateDaoImpl;
import com.peterphi.servicemanager.service.db.entity.LetsEncryptAccountEntity;
import com.peterphi.servicemanager.service.db.entity.LetsEncryptCertificateEntity;
import com.peterphi.servicemanager.service.dns.AzureDNS;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.types.SimpleId;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.RegistrationBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeConflictException;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.CertificateUtils;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Singleton
public class LetsEncryptService
{
	private static final Logger log = Logger.getLogger(LetsEncryptService.class);

	/**
	 * Size of RSA keypair to generate for Let's Encrypt account registration
	 */
	private static final int REGISTRATION_KEY_SIZE = 4096;
	/**
	 * Size of RSA keypair to generate for server certificates
	 */
	private static final int DOMAIN_KEY_SIZE = 2048;

	@Inject
	public AzureDNS dns;

	@Inject
	public HibernateDao<LetsEncryptAccountEntity, Integer> accountDao;

	@Inject
	public LetsEncryptCertificateDaoImpl certificateDao;

	@Inject(optional = true)
	@Named("acmeca.server-uri")
	@Doc("The ACME server URI - e.g. acme://letsencrypt.org or acme://letsencrypt.org/staging. Default acme://letsencrypt.org")
	public String acmeServerUri = "acme://letsencrypt.org";

	private transient Registration _registration;


	public Registration getRegistration()
	{
		if (_registration == null)
		{
			LetsEncryptAccountEntity existing = accountDao.getById(LetsEncryptAccountEntity.MAIN_ACCOUNT_ID);

			final KeyPair keypair;

			try
			{
				if (existing != null)
				{
					ByteArrayInputStream bis = new ByteArrayInputStream(existing.getKeypair());
					InputStreamReader r = new InputStreamReader(bis, StandardCharsets.UTF_8);

					keypair = KeyPairUtils.readKeyPair(r);
				}
				else
				{
					keypair = KeyPairUtils.createKeyPair(REGISTRATION_KEY_SIZE);

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					OutputStreamWriter w = new OutputStreamWriter(bos, StandardCharsets.UTF_8);

					KeyPairUtils.writeKeyPair(keypair, w);

					existing = new LetsEncryptAccountEntity();
					existing.setId(LetsEncryptAccountEntity.MAIN_ACCOUNT_ID);
					existing.setKeypair(bos.toByteArray());

					// Save the generated keypair
					accountDao.save(existing);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error creating/loading/saving Let's Encrypt Registration Keypair", e);
			}

			Session session = new Session(acmeServerUri, keypair);

			Registration registration;
			{
				try
				{
					try
					{
						final RegistrationBuilder registrationBuilder = new RegistrationBuilder();

						registration = registrationBuilder.create(session);
					}
					catch (AcmeConflictException ex)
					{
						registration = Registration.bind(session, ex.getLocation());
					}

					// Automatically accept any agreement updates
					registration.modify().setAgreement(registration.getAgreement()).commit();
				}
				catch (Exception e)
				{
					throw new RuntimeException("Unexpected error registering with ACME CA", e);
				}
			}

			_registration = registration;
		}

		return _registration;
	}


	public LetsEncryptCertificateEntity renew(final String domains)
	{
		final LetsEncryptCertificateEntity entity = getCertificate(domains);

		if (entity != null)
			return issue(domains);
		else
			throw new IllegalArgumentException("Cannot renew certs for " + domains + ": no such cert exists currently!");
	}


	public LetsEncryptCertificateEntity getCertificate(final String domains)
	{
		return certificateDao.getById(domains);
	}


	/**
	 * Issue a certificate for a domain (or a set of domains)
	 *
	 * @param domains
	 *
	 * @return
	 */
	public LetsEncryptCertificateEntity issue(final String domains)
	{
		// First, prove that we own the domains
		for (String domain : domains.split(","))
			proveOwnership(domain);

		// Next, (re)generate certificates
		return generateOrRenewCertificate(domains);
	}


	public LetsEncryptCertificateEntity generateOrRenewCertificate(final String domains)
	{
		LetsEncryptCertificateEntity entity = getCertificate(domains);

		// If we already have a keypair for these domains we shouldn't regenerate it, only regenerate the cert
		final KeyPair domainKeyPair;
		final boolean isNew;
		try
		{
			if (entity != null)
			{
				final ByteArrayInputStream bis = new ByteArrayInputStream(entity.getKeypair());
				final InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);

				domainKeyPair = KeyPairUtils.readKeyPair(isr);
				isNew = false;
			}
			else
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStreamWriter osr = new OutputStreamWriter(bos, StandardCharsets.UTF_8);

				domainKeyPair = KeyPairUtils.createKeyPair(DOMAIN_KEY_SIZE);

				KeyPairUtils.writeKeyPair(domainKeyPair, osr);

				entity = new LetsEncryptCertificateEntity();
				entity.setId(domains);
				entity.setKeypair(bos.toByteArray());

				isNew = true;
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error serialising/deserialising keypair for domains " + domains, e);
		}

		final Registration registration = getRegistration();

		// Generate a CSR for the domains
		CSRBuilder csrb = new CSRBuilder();
		for (String domain : domains.split(","))
			csrb.addDomain(domain);

		try
		{
			csrb.sign(domainKeyPair);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error signing CSR for " + domains + " with domains keypair!", e);
		}

		// Request a signed certificate
		final Certificate certificate;
		try
		{
			certificate = registration.requestCertificate(csrb.getEncoded());
			log.info("Success! The certificate for domains " + domains + " has been generated!");
			log.info("Certificate URI: " + certificate.getLocation());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to encode CSR request for " + domains, e);
		}
		catch (AcmeException e)
		{
			throw new RuntimeException("Failed to request certificate from Let's Encrypt for " + domains, e);
		}

		// Download the certificate
		final X509Certificate cert;
		final X509Certificate[] chain;
		try
		{
			cert = certificate.download();
			chain = certificate.downloadChain();
		}
		catch (AcmeException e)
		{
			throw new RuntimeException("Error downloading certificate information for " +
			                           domains +
			                           " " +
			                           certificate.getLocation(), e);
		}

		// Write certificate only
		final byte[] certBytes;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter osr = new OutputStreamWriter(bos, StandardCharsets.UTF_8);

			CertificateUtils.writeX509Certificate(cert, osr);
			certBytes = bos.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error serialising Cert for " + domains, e);
		}

		// Write chain only
		byte[] chainBytes;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter osr = new OutputStreamWriter(bos, StandardCharsets.UTF_8);

			CertificateUtils.writeX509CertificateChain(chain, osr);
			chainBytes = bos.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error serialising Cert for " + domains, e);
		}

		entity.setCert(certBytes);
		entity.setChain(chainBytes);
		entity.setExpires(new DateTime(cert.getNotAfter()));

		// Make sure a management token is assigned
		if (entity.getManagementToken() == null)
			entity.setManagementToken(SimpleId.alphanumeric(32));

		if (isNew)
		{
			certificateDao.save(entity);
		}
		else
		{
			certificateDao.update(entity);
		}

		return entity;
	}


	@Retry
	public void proveOwnership(final String domain)
	{
		Registration registration = getRegistration();

		final Authorization authorization;

		try
		{
			authorization = registration.authorizeDomain(domain);
		}
		catch (AcmeException e)
		{
			throw new RuntimeException("Error creating authorisation for " + domain, e);
		}

		Dns01Challenge challenge = authorization.findChallenge(Dns01Challenge.TYPE);

		if (challenge == null)
			throw new RuntimeException("DNS Challenge is not available! Cannot prove we own " + domain);

		final String domainName = "_acme-challenge." + domain;
		log.debug("Create TXT record " + domainName + " with value: " + challenge.getDigest());

		// Create the TXT record
		dns.createDNSRecord(domainName, RecordType.TXT, challenge.getDigest());

		// Wait for a short time for the change to DNS records to propagate through Microsoft's system
		// N.B. there's no docs suggesting this is needed or that this is the right value, but Let's Encrypt challenges
		//      seem to fail more regularly against the live API without this wait
		new Timeout(5, TimeUnit.SECONDS).sleep();

		// Allow the CA to start checking the TXT record
		try
		{
			log.trace("Challenge status " + challenge.getStatus());
			challenge.trigger();
			log.trace("Challenge status " + challenge.getStatus());
		}
		catch (AcmeException e)
		{
			throw new RuntimeException("Error triggering authorisation for " + domain, e);
		}


		// Poll waiting for the challenge to complete
		int attempts = 10;
		for (int attempt = 0; attempt < 10; attempt++)
		{
			log.trace("Challenge status " + challenge.getStatus());

			if (challenge.getStatus() == Status.INVALID)
				break;
			else if (challenge.getStatus() == Status.VALID)
				break;

			Timeout.TEN_SECONDS.sleep();

			try
			{
				challenge.update();
			}
			catch (AcmeException e)
			{
				log.warn("Error updating challenge", e);
			}
		}

		log.trace("Challenge status " + challenge.getStatus());

		dns.deleteDNSRecord(domainName, RecordType.TXT);

		if (challenge.getStatus() != Status.VALID)
		{
			throw new RuntimeException("Challenge " +
			                           challenge +
			                           " failed for " +
			                           domainName +
			                           "! Failed with state " +
			                           challenge.getStatus());
		}
		else
		{
			log.debug("Challenge " + challenge + " passed!");
		}
	}
}
