package com.peterphi.std.crypto.keygen;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.TimeZone;

public class RSAGenerator
{
	private static final Logger log = Logger.getLogger(RSAGenerator.class);

	static
	{
		if (Security.getProvider("BC") == null)
		{
			log.info("[RSAGenerator] Loading Bouncy Castle Provider");
			Security.addProvider(new BouncyCastleProvider());
			log.debug("[RSAGenerator] Bouncy Castle Provider loaded");
		}
	}


	// Prevent instantiation
	private RSAGenerator()
	{
	}


	public static KeyPair generate(int keybits) throws Exception
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
		generator.initialize(keybits, new SecureRandom());
		KeyPair kp = generator.generateKeyPair();

		return kp;
	}


	public byte[] createRequest(KeyPair kp, X500Principal subject) throws Exception
	{
		PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest("SHA512withRSA",
		                                                                  subject,
		                                                                  kp.getPublic(),
		                                                                  null,
		                                                                  kp.getPrivate());

		return kpGen.getEncoded();
	}


	public static X509Certificate createSimpleX509(String issueDN, String subjectDN, KeyPair kp, int validYears) throws Exception
	{
		X509V3CertificateGenerator gen = new X509V3CertificateGenerator();

		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar expires = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expires.add(Calendar.YEAR, validYears); // Certificate is valid for n year

		gen.setNotBefore(now.getTime());
		gen.setNotAfter(expires.getTime());

		gen.setPublicKey(kp.getPublic());
		gen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		gen.setSubjectDN(new X509Name(subjectDN));
		gen.setIssuerDN(new X509Name(issueDN));

		gen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		gen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature |
		                                                             KeyUsage.keyEncipherment |
		                                                             KeyUsage.dataEncipherment |
		                                                             KeyUsage.keyCertSign));
		gen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

		gen.setSignatureAlgorithm("SHA256WithRSAEncryption");
		X509Certificate cert = gen.generate(kp.getPrivate(), "BC");

		return cert;
	}
}
