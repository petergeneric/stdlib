package com.mediasmiths.std.crypto.keystore;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.io.*;

public class DERUtil {
	private DERUtil() {
	}


	public static X509Certificate parseX509(byte[] x509) throws CertificateException {
		final CertificateFactory factory = CertificateFactory.getInstance("x509");
		final X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(x509));

		return cert;
	}


	public static List<X509Certificate> parseX509(Iterable<byte[]> encoded) throws CertificateException {
		final List<X509Certificate> parsed = new ArrayList<X509Certificate>();

		for (byte[] cert : encoded)
			parsed.add(parseX509(cert));

		return parsed;
	}


	public static RSAPrivateKey parseRSAPrivateKey(byte[] bytes) throws GeneralSecurityException {
		return (RSAPrivateKey) parsePrivateKey("RSA", bytes);
	}


	public static PrivateKey parsePrivateKey(final String algorithm, byte[] bytes) throws GeneralSecurityException {
		PKCS8EncodedKeySpec encoded = new PKCS8EncodedKeySpec(bytes);

		final KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePrivate(encoded);
	}
}
