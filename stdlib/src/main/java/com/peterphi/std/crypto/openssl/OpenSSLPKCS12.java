package com.peterphi.std.crypto.openssl;

import com.peterphi.std.system.exec.Exec;
import com.peterphi.std.system.exec.Execed;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class OpenSSLPKCS12
{
	private static final Logger log = Logger.getLogger(OpenSSLPKCS12.class);

	public static final String OPENSSL = "openssl";
	public static final boolean USE_GENERIC_TEMP_DIRECTORY = false;


	/**
	 * Recreates a PKCS12 KeyStore using OpenSSL; this is a workaround a BouncyCastle-Firefox compatibility bug
	 *
	 * @param p12
	 * 		The PKCS12 Keystore to filter
	 * @param p12Password
	 * 		The password for the keystore
	 *
	 * @throws IOException
	 * 		if a catastrophic unexpected failure occurs during execution
	 * @throws IllegalArgumentException
	 * 		if the PKCS12 keystore doesn't exist
	 * @throws IllegalStateException
	 * 		if openssl exits with a failure condition
	 */
	public static void filterP12(File p12, String p12Password) throws IOException
	{
		if (!p12.exists())
			throw new IllegalArgumentException("p12 file does not exist: " + p12.getPath());

		final File pem;
		if (USE_GENERIC_TEMP_DIRECTORY)
			pem = File.createTempFile(UUID.randomUUID().toString(), "");
		else
			pem = new File(p12.getAbsolutePath() + ".pem.tmp");

		final String pemPassword = UUID.randomUUID().toString();

		try
		{
			P12toPEM(p12, p12Password, pem, pemPassword);
			PEMtoP12(pem, pemPassword, p12, p12Password);
		}
		finally
		{
			if (pem.exists())
				if (!pem.delete())
					log.warn("[OpenSSLPKCS12] {filterP12} Could not delete temporary PEM file " + pem);

		}
	}


	/**
	 * @param p12
	 * @param p12Password
	 * @param toPEM
	 * @param pemPassword
	 *
	 * @throws IOException
	 * 		if a catastrophic unexpected failure occurs during execution
	 * @throws IllegalArgumentException
	 * 		if the PKCS12 keystore doesn't exist
	 * @throws IllegalStateException
	 * 		if openssl exits with a failure condition
	 */
	public static void P12toPEM(File p12, String p12Password, File toPEM, String pemPassword) throws IOException
	{
		if (!p12.exists())
			throw new IllegalArgumentException("p12 file does not exist: " + p12.getPath());

		Execed openssl = Exec.utilityAs(null,
		                                OPENSSL,
		                                "pkcs12",
		                                "-in",
		                                p12.getPath(),
		                                "-out",
		                                toPEM.getPath(),
		                                "-passin",
		                                "pass:" + p12Password,
		                                "-nodes");

		int returnCode = openssl.waitForExit();

		if (returnCode != 0)
			throw new IllegalStateException("Unexpected openssl exit code " + returnCode + "; output:\n" +
			                                openssl.getStandardOut());
	}


	/**
	 * @param pem
	 * 		the PEM file containing the keys & certificates to put in a P12
	 * @param pemPassword
	 * 		The password for any encrypted keys in the PEM
	 * @param toP12
	 * 		The PKCS12 file
	 * @param p12Password
	 * 		the password to put on the PKCS12 keystore
	 *
	 * @throws IOException
	 * 		if a catastrophic unexpected failure occurs during execution
	 * @throws IllegalArgumentException
	 * 		if the PEM keystore doesn't exist
	 * @throws IllegalStateException
	 * 		if openssl exits with a failure condition
	 */
	public static void PEMtoP12(File pem, String pemPassword, File toP12, String p12Password) throws IOException
	{
		if (!pem.exists())
			throw new IllegalArgumentException("pem file does not exist: " + pem.getPath());

		Execed openssl = Exec.utilityAs(null,
		                                OPENSSL,
		                                "pkcs12",
		                                "-nodes",
		                                "-in",
		                                pem.getPath(),
		                                "-out",
		                                toP12.getPath(),
		                                "-export",
		                                "-passin",
		                                "pass:" + pemPassword,
		                                "-passout",
		                                "pass:" + p12Password);
		int returnCode = openssl.waitForExit();

		if (returnCode != 0)
			throw new IllegalStateException("Unexpected openssl exit code " + returnCode + "; output:\n" +
			                                openssl.getStandardOut());
	}
}
