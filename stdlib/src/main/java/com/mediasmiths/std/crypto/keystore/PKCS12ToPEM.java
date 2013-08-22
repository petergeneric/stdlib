package com.mediasmiths.std.crypto.keystore;

import java.security.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;

import com.mediasmiths.std.util.ListUtility;

/**
 * Assists in the conversion of PKCS12 files to PEM files.<br />
 * 
 * 
 * 
 */
public class PKCS12ToPEM {
	private static transient final Logger log = Logger.getLogger(PKCS12ToPEM.class);

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			log.debug("[PKCS12ToPEM] {static} Initialising BC Provider");
			Security.addProvider(new BouncyCastleProvider());
		}
	}


	public static void convert(File pkcs12, File certPem, File keyPem) throws Exception {
		convert(pkcs12, new char[] {}, new char[] {}, certPem, keyPem);
	}


	public static void convert(File pkcs12, char[] pkcs12Password, File certPem, File keyPem) throws Exception {
		convert(pkcs12, pkcs12Password, new char[] {}, certPem, keyPem);
	}


	/**
	 * Converts a PKCS12 to a PEM using the Bouncy Castle PKCS12 drivers
	 * 
	 * @param pkcs12 The PKCS12 (.p12) file
	 * @param pkcs12Password The password on the file (if unspecified, set to <code>new char[] {}</code>)
	 * @param keyPassword The password on the key entry (if unspecified, set to <code>new char[] {}</code>)
	 * @param certPem The certificate PEM (output file)
	 * @param keyPem The key PEM (output file)
	 * @throws Exception If any SecurityException, etc. is thrown
	 */
	public static void convert(File pkcs12, char[] pkcs12Password, char[] keyPassword, File certPem, File keyPem)
			throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
		ks.load(new FileInputStream(pkcs12), pkcs12Password);

		PEMWriter cert = new PEMWriter(new FileWriter(certPem));
		PEMWriter key = new PEMWriter(new FileWriter(keyPem));

		for (String alias : ListUtility.iterate(ks.aliases())) {
			boolean isKey = ks.isKeyEntry(alias);

			if (isKey) {
				Key k = ks.getKey(alias, keyPassword);
				cert.writeObject(ks.getCertificate(alias));
				key.writeObject(k);
			}
		}

		cert.close();
		key.close();
	}
}
