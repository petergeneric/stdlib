package com.peterphi.std.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * PEM handling utilities
 */
public final class PEMHelper
{
	private PEMHelper()
	{
	}


	/**
	 * Load one or more X.509 Certificates from a PEM file
	 *
	 * @param pemFile A PKCS8 PEM file containing only <code>CERTIFICATE</code> / <code>X.509 CERTIFICATE</code> blocks
	 * @return a JKS KeyStore with the certificate aliases "cert<code>index</code>" where index is the 0-based index of the
	 * certificate in the PEM
	 * @throws RuntimeException if a problem occurs
	 */
	public static KeyStore loadCertificates(final File pemFile)
	{
		try (final FileInputStream pem = new FileInputStream(pemFile))
		{
			final KeyStore ks = createEmptyKeyStore();

			int i = 0;
			for (Certificate cert : CertificateFactory.getInstance("X.509").generateCertificates(pem))
				ks.setCertificateEntry("cert" + (i++), cert);

			return ks;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error parsing PEM " + pemFile, e);
		}
	}


	private static KeyStore createEmptyKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
	{
		final KeyStore ks = KeyStore.getInstance("JKS");

		// Initialise as empty
		ks.load(null);

		return ks;
	}
}
