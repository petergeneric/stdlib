package com.mediasmiths.std.crypto.keystore;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.io.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

/**
 * Helps load key/trust material from various on-disk representations<br />
 * Requires the BouncyCastle library (which is registered if it's not already available)
 */
public final class KeystoreHelper {
	private static transient final Logger log = Logger.getLogger(KeystoreHelper.class);

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			log.debug("Initialising BouncyCastle JSSE Provider");
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private KeystoreHelper() {
	}


	// ///////////////////////////////////////////////////
	// public parse methods: static interface for loading key and truststores
	// //////////////////////////////////////////////////

	/**
	 * Loads key material from a PEM-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyPEM(final File file) throws GeneralSecurityException {
		return parsePEM(file, false);
	}


	/**
	 * Loads trust material from a PEM-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustPEM(final File file) throws GeneralSecurityException {
		return parsePEM(file, true);
	}


	/**
	 * Loads key material from a PEM-encoded Reader
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyPEM(final Reader reader) throws GeneralSecurityException {
		return loadPEM(reader, false);
	}


	/**
	 * Loads key/trust material from a PEM-encoded Reader
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustPEM(final Reader reader) throws GeneralSecurityException {
		return loadPEM(reader, true);
	}


	/**
	 * Loads key material from a JKS-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyJKS(final File file) throws GeneralSecurityException {
		return parseJKS(file);
	}


	/**
	 * Loads trust material from a JKS-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustJKS(final File file) throws GeneralSecurityException {
		return parseJKS(file);
	}


	/**
	 * Loads key material from a JKS-encoded InputStream
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyJKS(final InputStream file) throws GeneralSecurityException {
		return parseJKS(file);
	}


	/**
	 * Loads trust material from a JKS-encoded InputStream
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustJKS(final InputStream file) throws GeneralSecurityException {
		return parseJKS(file);
	}


	/**
	 * Loads key material from a PKCS12-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyP12(final File file) throws GeneralSecurityException {
		return parseP12(file);
	}


	/**
	 * Loads trust material from a PKCS12-encoded file
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustP12(final File file) throws GeneralSecurityException {
		return parseP12(file);
	}


	/**
	 * Loads key material from a PKCS12-encoded InputStream
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseKeyP12(final InputStream file) throws GeneralSecurityException {
		return parseP12(file);
	}


	/**
	 * Loads trust material from a PKCS12-encoded InputStream
	 * 
	 * @param file the source of the key/trust material
	 * @throws GeneralSecurityException if there is an IO problem or if the key/trust material is corrupt
	 * @returns a Java KeyStore representing the loaded key/trust material
	 */
	public static KeyStore parseTrustP12(final InputStream file) throws GeneralSecurityException {
		return parseP12(file);
	}


	// ///////////////////////////////////////////////////
	// core parse methods: implement the wrappers for the public methods
	// //////////////////////////////////////////////////

	private static KeyStore parsePEM(final File file, final boolean trust) throws GeneralSecurityException {
		try {
			final Reader reader = new FileReader(file);
			try {
				return parsePEM(reader, trust);
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			throw new KeyStoreException("Error reading from store file " + file + ": " + e.getMessage(), e);
		}
	}


	private static KeyStore parsePEM(final Reader reader, final boolean trust) throws GeneralSecurityException {
		return loadPEM(reader, trust);
	}


	private static KeyStore parseJKS(File file) throws GeneralSecurityException {
		try {
			final InputStream stream = new FileInputStream(file);
			try {
				return parseJKS(stream);
			}
			finally {
				stream.close();
			}
		}
		catch (IOException e) {
			throw new KeyStoreException("Error reading from JKS store file " + file + ": " + e.getMessage(), e);
		}
	}


	private static KeyStore parseJKS(final InputStream is) throws GeneralSecurityException {
		final char[] password = new char[0];

		return loadJKS(is, password);
	}


	private static KeyStore parseP12(final File file) throws GeneralSecurityException {
		try {
			final InputStream stream = new FileInputStream(file);
			try {
				return parseP12(stream);
			}
			finally {
				stream.close();
			}
		}
		catch (IOException e) {
			throw new KeyStoreException("Error reading from P12 store file " + file + ": " + e.getMessage(), e);
		}
	}


	private static KeyStore parseP12(final InputStream file) throws GeneralSecurityException {
		final char[] password = new char[0];

		return loadP12(file, password);
	}


	// ///////////////////////////////////////////////////
	// load methods: do the work of loading a KeyStore
	// //////////////////////////////////////////////////

	private static KeyStore loadJKS(InputStream stream, final char[] password) throws GeneralSecurityException {
		try {
			final KeyStore store = KeyStore.getInstance("JKS");

			store.load(stream, password);

			return store;
		}
		catch (IOException e) {
			throw new KeyStoreException("Error loading JKS from stream " + stream + ": " + e.getMessage(), e);
		}
	}


	private static KeyStore loadP12(InputStream stream, final char[] password) throws GeneralSecurityException {
		try {
			final KeyStore store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);

			store.load(stream, password);

			return store;
		}
		catch (IOException e) {
			throw new KeyStoreException("Error loading P12 from stream " + stream + ": " + e.getMessage(), e);
		}
	}


	private static KeyStore loadPEM(Reader pem, final boolean trust) throws GeneralSecurityException {
		try {
			// Set up a new KeyStore
			KeyStore store = KeyStore.getInstance("JKS");
			store.load(null, new char[] {});

			PEMReader reader = new PEMReader(pem);
			try {
				// Read the certificates and KeyPairs from the PEM
				List<X509Certificate> certs = new ArrayList<X509Certificate>();
				List<KeyPair> keys = new ArrayList<KeyPair>();
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
					List<X509Certificate> added = new ArrayList<X509Certificate>();
					// Keystore
					for (KeyPair key : keys) {
						for (X509Certificate cert : certs) {
							if (equals(cert.getPublicKey(), key.getPublic())) {
								final String alias = getAlias(cert);
	
								Certificate[] chain = new Certificate[] { cert };
	
								store.setCertificateEntry(alias, cert);
								store.setKeyEntry(alias, key.getPrivate(), new char[] {}, chain);
	
								added.add(cert);
							}
						}
	
						// Remove any user certificates that we've handled from the "certs" list
						certs.removeAll(added);
					}
				}
	
				// Add CA certificates, etc. to the store
				for (X509Certificate cert : certs) {
					final String alias = getAlias(cert);
	
					// Truststore
					store.setCertificateEntry(alias, cert);
				}
			}
			finally {
				IOUtils.closeQuietly(reader);
			}
			return store;
		}
		catch (IOException e) {
			throw new KeyStoreException("Error reading PEM from Reader " + pem + ": " + e.getMessage(), e);
		}
	}


	private static String getAlias(X509Certificate cert) {
		final String hexSerial = cert.getSerialNumber().toString(16);
		final String certType = cert.getType();

		final String alias = certType + "_" + hexSerial;

		return alias;
	}


	private static boolean equals(PublicKey a, PublicKey b) {
		return Arrays.equals(a.getEncoded(), b.getEncoded());
	}

}
