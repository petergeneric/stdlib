package com.peterphi.std.crypto;

import org.apache.log4j.Logger;

import javax.security.auth.x500.X500Principal;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class which helps manage certificate chains
 */
public class CertificateChainUtil
{
	private static transient final Logger log = Logger.getLogger(CertificateChainUtil.class);
	public static final boolean ALLOW_LOG_SELF_SIGN_TESTS = false;


	private CertificateChainUtil()
	{
	}


	/**
	 * Extracts the DNs of the issuers from a certificate chain. The last certificate in the chain will be ignored (since it is
	 * the subject)
	 *
	 * @param chain
	 * 		a normalised chain
	 *
	 * @return
	 */
	public static X500Principal[] getIssuerDNsFromChain(List<X509Certificate> chain)
	{
		if (chain == null || chain.isEmpty())
			throw new IllegalArgumentException("Must provide a chain that is non-null and non-empty");

		// Given a chain of n, there are n-1 values for "issuers" and 1 "subject"
		final X500Principal[] issuers = new X500Principal[chain.size() - 1]; // Allocate array of n-1 for issuers

		for (int i = 0; i < issuers.length; i++)
		{
			final X509Certificate certificate = chain.get(i);
			final X500Principal subject = certificate.getSubjectX500Principal();

			issuers[i] = subject;
		}

		return issuers;
	}


	/**
	 * Extracts the Subject: the final certificate in a chain
	 *
	 * @param chain
	 * 		a normalised chain
	 *
	 * @return the subject (the final certificate in the chain)
	 *
	 * @throws IllegalArgumentException
	 * 		if the chain is null or empty
	 */
	public static X500Principal getSubjectDNFromChain(List<X509Certificate> chain)
	{
		if (chain == null || chain.isEmpty())
			throw new IllegalArgumentException("Must provide a chain that is non-null and non-empty");

		X509Certificate cert = chain.get(chain.size() - 1);

		return cert.getSubjectX500Principal();
	}


	/**
	 * Determines if a certificate is a self signed certificate
	 *
	 * @param certificate
	 * 		the certificate to test
	 *
	 * @return true if the certificate is self-signed, otherwise false if the certificate was not self-signed or the certificate
	 * signature could not be verified
	 */
	public static boolean isSelfSigned(X509Certificate certificate)
	{
		return isSignedBy(certificate, certificate.getPublicKey());
	}


	@SuppressWarnings("unused")
	public static boolean isSignedBy(X509Certificate subject, PublicKey signer)
	{
		try
		{
			subject.verify(signer);

			// if verify does not throw an exception then it's a self-signed certificate
			return true;
		}
		catch (Exception e)
		{
			if (ALLOW_LOG_SELF_SIGN_TESTS && log.isTraceEnabled())
			{
				final String dn = subject.getIssuerX500Principal().getName();

				log.trace("{isSignedBy} " + dn + " not signed by " + signer + ":" + e.getMessage(), e);
			}

			return false;
		}

	}


	public static List<X509Certificate> buildChainFor(PublicKey key, Collection<X509Certificate> certs)
	{
		final List<X509Certificate> chain = new ArrayList<X509Certificate>(certs.size());

		final X509Certificate subject = getCertificateFor(key, certs);

		if (subject == null)
			throw new IllegalArgumentException("Cannot find X509Certificate which corresponds to " + key);

		chain.add(subject);

		// Keep going until we find a root certificate (or until the chain can't be continued)
		{
			X509Certificate old = null;
			X509Certificate current = subject;
			while (current != null && (old == null || !old.equals(current)) && !isSelfSigned(current))
			{
				old = current;
				current = getIssuer(current, certs);

				if (current != null)
					chain.add(current);
				else
				{
					log.warn("{buildChainFor} Building chain for " +
					         certs.size() +
					         " cert[s] but had to stop after " +
					         chain.size() +
					         " because I could not find the issuer for " +
					         old.getSubjectX500Principal());
					throw new IllegalArgumentException("Could not determine issuer for certificate: " +
					                                   old.getSubjectX500Principal() +
					                                   ". Please ensure certificate list contains all certificates back to the CA's self-signed root!");
				}

				if (chain.size() > certs.size())
				{
					log.warn("{buildChainFor} Too many certificates in chain. Chain: " + Arrays.toString(getPrincipals(chain)) +
					         ", Source: " + Arrays.toString(getPrincipals(new ArrayList<X509Certificate>(certs))));

					throw new IllegalStateException("Chain build failed: too many certs in chain (greater than number of input certs)! Chain: " +
					                                Arrays.toString(getPrincipals(chain)));
				}
			}
		}

		// Normalise the array
		return normaliseChain(chain);
	}


	public static List<X509Certificate> buildChainFor(KeyPair keypair, Collection<X509Certificate> certs)
	{
		return buildChainFor(keypair.getPublic(), certs);
	}


	public static X500Principal[] getPrincipals(List<X509Certificate> chain)
	{
		if (chain.contains(null))
			throw new IllegalArgumentException("Certificate chain contains null!");

		X500Principal[] array = new X500Principal[chain.size()];

		for (int i = 0; i < array.length; i++)
			array[i] = chain.get(i).getSubjectX500Principal();

		return array;
	}


	public static X509Certificate getCertificateFor(PublicKey publicKey, Collection<X509Certificate> certs)
	{
		// Search through the certs until we find the public key we're looking for
		for (X509Certificate cert : certs)
		{
			if (cert.getPublicKey().equals(publicKey))
				return cert;
		}

		return null;
	}


	public static X509Certificate getIssuer(X509Certificate subject, Collection<X509Certificate> certs)
	{
		for (X509Certificate cert : certs)
		{
			if (cert.getSubjectX500Principal().equals(subject.getIssuerX500Principal()))
			{
				if (isSignedBy(subject, cert.getPublicKey()))
				{
					return cert;
				}
			}
		}

		return null;
	}


	/**
	 * Take a chain and return a (Read-only) chain with the root certificate as the first entry
	 *
	 * @param chain
	 * 		a chain with the certificates in order (either leading away from root or leading towards root)
	 *
	 * @return a read-only chain leading away from the root certificate
	 *
	 * @throws IllegalArgumentException
	 * 		if the chain is null or empty
	 */
	public static List<X509Certificate> normaliseChain(List<X509Certificate> chain)
	{
		return toRootFirst(chain);
	}


	/**
	 * Take a chain and return a (Read-only) chain with the root certificate as the first entry
	 *
	 * @param chain
	 * 		a chain with the certificates in order (either leading away from root or leading towards root)
	 *
	 * @return a read-only chain leading away from the root certificate
	 *
	 * @throws IllegalArgumentException
	 * 		if the chain is null or empty
	 */
	public static List<X509Certificate> toRootFirst(List<X509Certificate> chain)
	{
		if (chain == null || chain.isEmpty())
			throw new IllegalArgumentException("Must provide a chain that is non-null and non-empty");

		final List<X509Certificate> out;
		// Sort the list so the root certificate comes first
		if (!isSelfSigned(chain.get(0)))
		{
			// Copy the chain List so we can modify it
			out = new ArrayList<X509Certificate>(chain);

			Collections.reverse(out);

			// If, even when reversed, the chain doesn't have a root at the start then the chain's invalid
			if (!isSelfSigned(out.get(0)))
			{
				throw new IllegalArgumentException("Neither end of the certificate chain has a Root! " + chain);
			}
		}
		else
		{
			out = chain;
		}

		return Collections.unmodifiableList(out);
	}


	/**
	 * Take a chain and return a (Read-only) chain with the root certificate as the last entry
	 *
	 * @param chain
	 * 		a chain with the certificates in order (either leading away from root or leading towards root)
	 *
	 * @return a read-only chain leading towards the root certificate (i.e. with the root certificate
	 *
	 * @throws IllegalArgumentException
	 * 		if the chain is null or empty
	 */
	public static List<X509Certificate> toRootLast(List<X509Certificate> chain)
	{
		if (chain == null || chain.isEmpty())
			throw new IllegalArgumentException("Must provide a chain that is non-null and non-empty");

		final List<X509Certificate> out;
		// Sort the list so the root certificate comes last
		if (!isSelfSigned(chain.get(chain.size() - 1)))
		{
			// Copy the chain List so we can modify it
			out = new ArrayList<X509Certificate>(chain);

			Collections.reverse(out);

			// If, even when reversed, the chain doesn't have a root at the start then the chain's invalid
			if (!isSelfSigned(out.get(out.size() - 1)))
			{
				throw new IllegalArgumentException("Neither end of the certificate chain has a Root! " + chain);
			}
		}
		else
		{
			out = chain;
		}

		return Collections.unmodifiableList(out);
	}


	/**
	 * Verifies that a certificate chain is valid
	 *
	 * @param chain
	 * 		a certificate chain with the root certificate first
	 *
	 * @throws IllegalArgumentException
	 * 		if the chain is invalid, null or empty
	 */
	public static void verifyChain(List<X509Certificate> chain)
	{
		if (chain == null || chain.isEmpty())
			throw new IllegalArgumentException("Must provide a chain that is non-null and non-empty");

		for (int i = 0; i < chain.size(); i++)
		{
			final X509Certificate certificate = chain.get(i);
			final int issuerIndex = (i != 0) ?
			                        i - 1 :
			                        0; // The index of the issuer is the previous cert (& the root must, of course, sign itself)
			final X509Certificate issuer = chain.get(issuerIndex);

			// Verify the certificate was indeed issued by the previous certificate in the chain
			try
			{
				certificate.verify(issuer.getPublicKey());
			}
			catch (GeneralSecurityException e)
			{
				final String msg = "Failure verifying " + certificate + " against claimed issuer " + issuer;

				throw new IllegalArgumentException(msg + ": " + e.getMessage(), e);
			}
		}
	}

	/*
		public static void main(String[] args) throws Exception {
			if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
				Security.addProvider(new BouncyCastleProvider());

			loadStore(new FileReader("/path/to/pem"), false);
		}


		protected static KeyStore loadStore(Reader pem, final boolean trust) throws Exception {

			// Set up a new KeyStore
			KeyStore store = KeyStore.getInstance("JKS");
			store.load(null, new char[] {});

			PEMReader reader = new PEMReader(pem);

			// Read the certificates and KeyPairs from the PEM
			List<X509Certificate> certs = new ArrayList();
			List<KeyPair> keys = new ArrayList();
			{
				Object obj;
				do {
					obj = reader.readObject();

					if (obj != null) {
						if (obj instanceof X509Certificate) {
							certs.add((X509Certificate) obj);
						}
						else if (obj instanceof KeyPair) {
							keys.add((KeyPair) obj);
						}
						else {
							log.warn("{loadStore} Unknown object in PEM: " + obj.getClass() + ". " + obj.toString() + ". Ignoring.");
						}
					}
				} while (obj != null);
			}

			if (keys.isEmpty() && !trust) {
				throw new IllegalArgumentException("Loading key PEM but there were no KeyPairs found in the PEM!");
			}
			else if (certs.isEmpty()) {
				throw new IllegalArgumentException("No certificates found!");
			}

			if (!trust) {
				// Keystore
				for (KeyPair key : keys) {
					X509Certificate subject = CertificateChainUtil.getCertificateFor(key.getPublic(), certs);

					// new Certificate[]
					List<X509Certificate> chain = CertificateChainUtil.buildChainFor(key, certs);
					X509Certificate[] chainArray = chain.toArray(new X509Certificate[chain.size()]);

					// Add all the certificates in use
					for (X509Certificate cert : chain) {
						final String alias = getAlias(cert);

						if (!store.containsAlias(alias)) {
							store.setCertificateEntry(alias, cert);
						}
					}

					final String alias = getAlias(subject);
					store.setKeyEntry(alias, key.getPrivate(), new char[] {}, chainArray);
				}
			}

			// Add CA certificates, etc. to the store
			for (X509Certificate cert : certs) {
				final String alias = getAlias(cert);

				// Truststore
				store.setCertificateEntry(alias, cert);
			}

			return store;
		}


		protected static String getAlias(X509Certificate cert) {
			final String hexSerial = cert.getSerialNumber().toString(16);
			final String certType = cert.getType();

			final String alias = certType + "_" + hexSerial;

			return alias;
		}

		/*
		// testing helper method
		private static X509Certificate getOneCertificate(File file) throws Exception {
			if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
				Security.addProvider(new BouncyCastleProvider());

			PEMReader r = new PEMReader(new FileReader(file), null, BouncyCastleProvider.PROVIDER_NAME);
			final Object o = r.readObject();
			// An X509 certificate
			if (o instanceof X509Certificate) {
				X509Certificate cert = (X509Certificate) o;
				cert.checkValidity();

				return cert;
			}
			else
				throw new IllegalArgumentException("Expected X509Certificate but got " + o);
		}
		// */
}
