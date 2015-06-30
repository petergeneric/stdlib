package com.peterphi.std.crypto;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * PEM handling utilities
 */
public class PEMHelper
{
	private PEMHelper()
	{
	}


	/**
	 * Load one or more X.509 Certificates from a PEM file
	 *
	 * @param pemFile
	 * 		A PKCS8 PEM file containing only <code>CERTIFICATE</code> / <code>X.509 CERTIFICATE</code> blocks
	 *
	 * @return a JKS KeyStore with the certificate aliases "cert<code>index</code>" where index is the 0-based index of the
	 * certificate in the PEM
	 *
	 * @throws RuntimeException
	 * 		if a problem occurs
	 */
	public static KeyStore loadCertificates(final File pemFile)
	{
		try (final PemReader pem = new PemReader(new FileReader(pemFile)))
		{
			final KeyStore ks = createEmptyKeyStore();

			int certIndex = 0;
			Object obj;
			while ((obj = parse(pem.readPemObject())) != null)
			{
				if (obj instanceof Certificate)
				{
					final Certificate cert = (Certificate) obj;

					ks.setCertificateEntry("cert" + Integer.toString(certIndex++), cert);
				}
				else
				{
					throw new RuntimeException("Unknown PEM contents: " + obj + ". Expected a Certificate");
				}
			}

			return ks;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error parsing PEM " + pemFile, e);
		}
	}


	/**
	 * Parse a PemObject. Currently only supports  <code>CERTIFICATE</code> / <code>X.509 CERTIFICATE</code> types
	 *
	 * @param obj
	 * 		a PemObject with a type and with contents
	 *
	 * @return a parsed object (or null if the input is null)
	 *
	 * @throws GeneralSecurityException
	 * 		if there is a parsing problem
	 * @throws IllegalArgumentException
	 * 		if the PemObject cannot be recognised
	 */
	public static Object parse(final PemObject obj) throws GeneralSecurityException
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj.getType() == null)
		{
			throw new RuntimeException("Encountered invalid PemObject with null type: " + obj);
		}
		else if (obj.getType().equalsIgnoreCase("CERTIFICATE") || obj.getType().equalsIgnoreCase("X.509 CERTIFICATE"))
		{
			return parseX509Certificate(obj);
		}
		else
		{
			throw new IllegalArgumentException("Unknown PEM contents: encountered unsupported entry of type " +
			                                   obj.getType() +
			                                   " (expected CERTIFICATE or X.509 CERTIFICATE)");
		}
	}


	private static Certificate parseX509Certificate(final PemObject obj) throws CertificateException
	{
		final CertificateFactory factory = CertificateFactory.getInstance("X.509");

		return factory.generateCertificate(new ByteArrayInputStream(obj.getContent()));
	}


	private static KeyStore createEmptyKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
	{
		final KeyStore ks = KeyStore.getInstance("JKS");

		// Initialise as empty
		ks.load(null);

		return ks;
	}
}
