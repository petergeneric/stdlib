package com.mediasmiths.std.crypto.keygen;

/*
 * Adapted from OpenInfoCard CertsAndKeys.java whose license reads:
 * 
 * Copyright (c) 2006, Axel Nennker - http://axel.nennker.de/ All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. The names of the contributors may NOT be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.mediasmiths.std.util.HexHelper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

public class CaHelper
{
	private static transient final Logger log = Logger.getLogger(CaHelper.class);


	//public static String getSignatureAlgorithm() = "SHA1WithRSAEncryption"; // Was SHA1withRSAEncryption
	/**
	 * If true, enables a hack which works around GT 4.2.1 "Bug 3299:12 Extended Key Usage certificate extension not supported" (a
	 * problem in CoG)
	 */
	public static final boolean GLOBUS_COG_HACK = true;

	/**
	 * If true, changes the default algorithm to MD5WITHRSA
	 */
	public static final boolean GLOBUS_ALGORITHM_HACK = true;


	private static boolean getExtendedKeyUsageCriticality()
	{
		if (GLOBUS_COG_HACK)
			return false; // Not critical
		else
			return true; // Critical extension
	}


	private static String getSignatureAlgorithm()
	{
		if (GLOBUS_ALGORITHM_HACK)
			return "MD5WITHRSA";
		else
			return "SHA1WithRSAEncryption";
	}


	static
	{
		if (Security.getProvider("BC") == null)
		{
			log.info("[CaHelper] Loading Bouncy Castle Provider");
			Security.addProvider(new BouncyCastleProvider());
			log.debug("[CaHelper] Bouncy Castle Provider loaded");
		}
	}


	public static String opensslHash(X509Certificate cert)
	{
		try
		{
			return openssl_X509_NAME_hash(cert.getSubjectX500Principal());
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new Error("MD5 isn't available!", e);
		}
	}


	/**
	 * Generates a hexidecimal OpenSSL X509_NAME hash (as used in openssl x509 -hash -in cert.pem)<br />
	 * Based on openssl's crypto/x509/x509_cmp.c line 321
	 *
	 * @param p
	 *
	 * @return
	 */
	public static String openssl_X509_NAME_hash(X500Principal p) throws NoSuchAlgorithmException
	{
		// DER-encode the Principal, MD5 hash it, then extract the first 4 bytes and reverse their positions
		// MAINTAINER NOTE: This code replicates OpenSSL's hashing function

		byte[] derEncodedSubject = p.getEncoded();

		byte[] md5 = MessageDigest.getInstance("MD5").digest(derEncodedSubject);

		// Reduce the MD5 hash to a single unsigned long
		byte[] result = new byte[]{md5[3], md5[2], md5[1], md5[0]};

		return HexHelper.toHex(result);
	}


	public final static DERObjectIdentifier netscapeCertType = new DERObjectIdentifier("2.16.840.1.113730.1.1");


	public static KeyPair generateKeyPair(int bits) throws Exception
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
		keyGen.initialize(bits, new SecureRandom());
		return keyGen.generateKeyPair();
	}


	/**
	 * @param certificatePublicKey
	 * @param caPrivateKey
	 * @param issuer
	 * @param subject
	 *
	 * @return
	 */
	public static X509Certificate generateClientCertificate(PublicKey certificatePublicKey,
	                                                        PrivateKey caPrivateKey,
	                                                        X509Name issuer,
	                                                        X509Name subject) throws Exception
	{

		X509Certificate cert = null;

		X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
		gen.setIssuerDN(issuer);
		setNotBeforeNotAfter(gen, 10); // validity from 48 hours in the past until 10 years in the future
		gen.setSubjectDN(subject);
		gen.setPublicKey(certificatePublicKey);
		gen.setSignatureAlgorithm(getSignatureAlgorithm());
		gen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		gen = addClientExtensions(gen);

		cert = gen.generate(caPrivateKey, "BC");
		return cert;
	}


	/**
	 * @param certificatePublicKey
	 * @param caPrivateKey
	 * @param issuer
	 * @param subject
	 *
	 * @return
	 */
	public static X509Certificate generateServerCertificate(PublicKey certificatePublicKey,
	                                                        PrivateKey caPrivateKey,
	                                                        X509Name issuer,
	                                                        X509Name subject) throws Exception
	{
		X509Certificate cert = null;

		X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
		gen.setIssuerDN(issuer);
		gen.setSubjectDN(subject);
		setNotBeforeNotAfter(gen, 10); // validity from 48 hours in the past until 10 years in the future
		gen.setPublicKey(certificatePublicKey);
		gen.setSignatureAlgorithm(getSignatureAlgorithm());
		gen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		gen = addSSLServerExtensions(gen);

		cert = gen.generate(caPrivateKey, "BC");
		return cert;
	}


	private static void setNotBeforeNotAfter(final X509V3CertificateGenerator gen, final int validForYears)
	{

		// Make sure the timezone is UTC (non-UTC timezones seem to cause PureTLS some problems, which causes globus problems)
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		now.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Set Not Before to be 48 hours in the past
		now.add(Calendar.HOUR, -48);
		gen.setNotBefore(now.getTime());


		// Set Not After to be 10 years after that
		now.add(Calendar.YEAR, validForYears);
		gen.setNotAfter(now.getTime());
	}


	public static X509Certificate generateCaCertificate(final String friendlyName,
	                                                    final KeyPair kp,
	                                                    final BigInteger serial,
	                                                    final X509Name issuer) throws Exception
	{
		return generateCaCertificate(friendlyName, kp, serial, issuer, issuer);
	}


	/**
	 * @param kp
	 * @param issuer
	 * @param subject
	 *
	 * @return
	 */
	public static X509Certificate generateCaCertificate(final String friendlyName,
	                                                    final KeyPair kp,
	                                                    final BigInteger serial,
	                                                    final X509Name issuer,
	                                                    final X509Name subject) throws Exception
	{

		X509Certificate cert = null;

		X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
		gen.setIssuerDN(issuer);
		setNotBeforeNotAfter(gen, 20); // The CA certificate is valid for 20 years
		gen.setSubjectDN(subject);
		gen.setPublicKey(kp.getPublic());
		gen.setSignatureAlgorithm(getSignatureAlgorithm());

		if (serial != null)
			gen.setSerialNumber(serial);
		else
			gen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));

		gen = addCaExtensions(gen, kp.getPublic());
		// gen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
		// new SubjectKeyIdentifierStructure(kp.getPublic()));
		cert = gen.generate(kp.getPrivate(), "BC");

		cert.checkValidity();
		cert.verify(kp.getPublic(), "BC");

		if (friendlyName != null)
		{
			PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) cert;
			bagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(friendlyName));
		}

		return cert;
	}


	public static PKCS10CertificationRequest generateCertificateRequest(X509Certificate cert,
	                                                                    PrivateKey signingKey) throws Exception
	{
		ASN1EncodableVector attributes = new ASN1EncodableVector();

		Set<String> nonCriticalExtensionOIDs = cert.getNonCriticalExtensionOIDs();
		for (String nceoid : nonCriticalExtensionOIDs)
		{
			byte[] derBytes = cert.getExtensionValue(nceoid);
			ByteArrayInputStream bis = new ByteArrayInputStream(derBytes);
			ASN1InputStream dis = new ASN1InputStream(bis);
			try
			{
				DERObject derObject = dis.readObject();
				DERSet value = new DERSet(derObject);
				Attribute attr = new Attribute(new DERObjectIdentifier(nceoid), value);
				attributes.add(attr);
			}
			finally
			{
				IOUtils.closeQuietly(dis);
			}
		}
		PKCS10CertificationRequest certificationRequest = new PKCS10CertificationRequest(getSignatureAlgorithm(),
		                                                                                 cert.getSubjectX500Principal(),
		                                                                                 cert.getPublicKey(),
		                                                                                 new DERSet(attributes),
		                                                                                 signingKey);
		return certificationRequest;
	}


	static private X509V3CertificateGenerator addCaExtensions(X509V3CertificateGenerator gen, PublicKey pubKey) throws Exception
	{
		gen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));
		gen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature |
		                                                             KeyUsage.keyEncipherment |
		                                                             KeyUsage.dataEncipherment |
		                                                             KeyUsage.keyCertSign |
		                                                             KeyUsage.cRLSign));

		gen.addExtension(X509Extensions.ExtendedKeyUsage,
		                 getExtendedKeyUsageCriticality(),
		                 new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
		// gen.addExtension(X509Extensions.SubjectAlternativeName, false,
		// new GeneralNames(new GeneralName(GeneralName.rfc822Name,
		// "test@test.test")));

		// netscape-cert-type "2.16.840.1.113730.1.1"
		// * bit-0 SSL client - 128
		// * bit-1 SSL server - 64
		// * bit-2 S/MIME - 32
		// * bit-3 Object Signing - 16
		// * bit-4 Reserved - 8
		// * bit-5 SSL CA - 4
		// * bit-6 S/MIME CA - 2
		// * bit-7 Object Signing CA - 1
		gen.addExtension(netscapeCertType, false, new DERBitString(new byte[]{Byte.MAX_VALUE})); // was 4

		addSubjectKeyIdentifier(gen, pubKey);
		addAuthorityKeyIdentifier(gen, pubKey);
		return gen;
	}


	@SuppressWarnings("unused")
	static private X509V3CertificateGenerator addServerExtensions(X509V3CertificateGenerator gen,
	                                                              PublicKey pubKey) throws Exception
	{
		gen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));
		gen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature |
		                                                             KeyUsage.keyEncipherment |
		                                                             KeyUsage.dataEncipherment));

		gen.addExtension(X509Extensions.ExtendedKeyUsage,
		                 getExtendedKeyUsageCriticality(),
		                 new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
		// gen.addExtension(X509Extensions.SubjectAlternativeName, false,
		// new GeneralNames(new GeneralName(GeneralName.rfc822Name,
		// "test@test.test")));

		// netscape-cert-type "2.16.840.1.113730.1.1"
		// * bit-0 SSL client - 128
		// * bit-1 SSL server - 64
		// * bit-2 S/MIME - 32
		// * bit-3 Object Signing - 16
		// * bit-4 Reserved - 8
		// * bit-5 SSL CA - 4
		// * bit-6 S/MIME CA - 2
		// * bit-7 Object Signing CA - 1

		gen.addExtension(netscapeCertType, false, new DERBitString(new byte[]{-16})); // was 4

		addSubjectKeyIdentifier(gen, pubKey);
		addAuthorityKeyIdentifier(gen, pubKey);
		return gen;
	}


	/**
	 * @param gen
	 * @param pubKey
	 *
	 * @throws IOException
	 */
	private static void addAuthorityKeyIdentifier(X509V3CertificateGenerator gen, PublicKey pubKey) throws Exception
	{
		{
			ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded()));
			try
			{
				SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) is.readObject());
				AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);

				gen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), false, aki);
			}
			finally
			{
				IOUtils.closeQuietly(is);
			}
		}
	}


	/**
	 * @param gen
	 * @param pubKey
	 *
	 * @throws IOException
	 */
	private static void addSubjectKeyIdentifier(X509V3CertificateGenerator gen, PublicKey pubKey) throws Exception
	{
		{
			ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded()));
			try
			{
				SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) is.readObject());
				SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
				gen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(), false, ski);
			}
			finally
			{
				IOUtils.closeQuietly(is);
			}
		}
	}


	static private X509V3CertificateGenerator addSSLServerExtensions(X509V3CertificateGenerator gen)
	{
		gen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		gen.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature));
		Vector<DERObjectIdentifier> extendedKeyUsageV = new Vector<DERObjectIdentifier>();
		extendedKeyUsageV.add(KeyPurposeId.id_kp_serverAuth);
		extendedKeyUsageV.add(KeyPurposeId.id_kp_clientAuth);
		// Netscape Server Gated Crypto
		// extendedKeyUsageV.add(new DERObjectIdentifier("2.16.840.1.113730.4.1"));
		// Microsoft Server Gated Crypto
		// extendedKeyUsageV
		// .add(new DERObjectIdentifier("1.3.6.1.4.1.311.10.3.3"));
		gen.addExtension(X509Extensions.ExtendedKeyUsage,
		                 getExtendedKeyUsageCriticality(),
		                 new ExtendedKeyUsage(extendedKeyUsageV));
		// gen.addExtension(X509Extensions.SubjectAlternativeName, false,
		// new GeneralNames(new GeneralName(GeneralName.rfc822Name,
		// "test@test.test")));
		// gen.addExtension(netscapeCertType, false, new DERBitString(
		// new byte[] { 64 }));

		return gen;
	}


	static private X509V3CertificateGenerator addClientExtensions(X509V3CertificateGenerator gen) throws Exception
	{
		gen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		gen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature |
		                                                             KeyUsage.keyEncipherment |
		                                                             KeyUsage.dataEncipherment |
		                                                             KeyUsage.keyCertSign));
		gen.addExtension(X509Extensions.ExtendedKeyUsage,
		                 getExtendedKeyUsageCriticality(),
		                 new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

		return gen;
	}


	public static void main(String[] args) throws Exception
	{

		String casubject = "C=UK, O=SOMEORG, OU=Org Unit, CN=Example Certificate Authority";


		X509Certificate cacert = null;
		PrivateKey caPrivateKey = null;

		if (true)
		{
			KeyStore ks = KeyStore.getInstance("PKCS12", "BC");

			ks.load(new FileInputStream(new File("/tmp/someorg-ca.p12")), new char[]{});
			caPrivateKey = (PrivateKey) ks.getKey("ca", new char[]{});

			cacert = (X509Certificate) ks.getCertificate("ca");
		}
		else
		{
			KeyPair cakeys = generateKeyPair(2048);
			caPrivateKey = cakeys.getPrivate();
			cacert = generateCaCertificate(casubject, cakeys, (BigInteger) null, new X509Name(casubject));
		}

		{
			// CA .p12
			{
				KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
				ks.load(null);
				//ks.setCertificateEntry("ca", cacert);
				ks.setKeyEntry("ca", caPrivateKey, new char[]{}, new java.security.cert.Certificate[]{cacert});

				ks.store(new FileOutputStream("/tmp/someorg-ca.p12"), new char[]{});
			}

			// CA .jks (public key only)
			{
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(null);
				ks.setCertificateEntry("ca", cacert);

				ks.store(new FileOutputStream("/tmp/ca-public.jks"), new char[]{});
			}

			// CA .pem (public key only)
			{
				PEMWriter pem = new PEMWriter(new FileWriter(new File("/tmp/d3ca.crt")));

				pem.writeObject(cacert);
				pem.close();
			}
		}

		/*
		// User
		{
			String user = "C=UK, O=SOMEORG, OU=Org Unit, L=SomeCompany, CN=Some User (test)";
			KeyPair keys = generateKeyPair(1024);
			X509Certificate cert = generateClientCertificate(keys.getPublic(), caPrivateKey, new X509Name(subject),
			    new X509Name(user));

			{
				KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
				ks.load(null);
				ks.setCertificateEntry("issuer", cacert);
				ks.setCertificateEntry("me", cert);
				ks.setKeyEntry("me", keys.getPrivate(), new char[] {}, new java.security.cert.Certificate[] { cert, cacert });

				ks.store(new FileOutputStream("/tmp/someorg-someuser.p12"), "SomeCompanysecurity".toCharArray());
			}

			{
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(null);
				ks.setKeyEntry("me", keys.getPrivate(), new char[] {}, new java.security.cert.Certificate[] { cert, cacert });
				// ks.setCertificateEntry("issuer", cacert);
				// ks.setCertificateEntry("me", cert);

				ks.store(new FileOutputStream("/tmp/someorg-someuser.jks"), new char[] {});
			}
		}//*/

		// examplehost hostkey:
		{
			String user = "C=UK, O=SOMEORG, OU=Org Unit, L=SomeCompany, CN=examplehost.example.mediasmithsforge.com";
			KeyPair keys = generateKeyPair(1024);
			X509Certificate cert = generateServerCertificate(keys.getPublic(),
			                                                 caPrivateKey,
			                                                 new X509Name(casubject),
			                                                 new X509Name(user));

			{
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(null);
				ks.setKeyEntry("me", keys.getPrivate(), new char[]{}, new java.security.cert.Certificate[]{cert, cacert});
				// ks.setCertificateEntry("issuer", cacert);
				// ks.setCertificateEntry("me", cert);

				ks.store(new FileOutputStream("/tmp/host.jks"), new char[]{});
			}

			{
				KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
				ks.load(null);
				ks.setCertificateEntry("issuer", cacert);
				ks.setCertificateEntry("me", cert);
				ks.setKeyEntry("me", keys.getPrivate(), new char[]{}, new java.security.cert.Certificate[]{cert, cacert});

				ks.store(new FileOutputStream("/tmp/host.p12"), new char[]{});
			}
		}
	}
}
