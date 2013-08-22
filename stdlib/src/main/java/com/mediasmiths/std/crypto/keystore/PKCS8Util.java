package com.mediasmiths.std.crypto.keystore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.PasswordFinder;

/**
 * PKCS8 Utilities
 */
public class PKCS8Util {
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}


	/**
	 * Takes a PEM-encoded non-password-protected PKCS8 key-containing file and returns the KeyPair within. Only the first keypair is considered
	 * 
	 * @return
	 * @throws IOException if the file is not a valid PKCS8 file
	 */

	public static KeyPair toKeyPair(final File file) throws IOException {
		return toKeyPair(file, null);
	}


	/**
	 * Takes a PEM-encoded PKCS8 key-containing file and returns the KeyPair within. Only the first keypair is considered
	 * 
	 * @return
	 * @throws IOException if the file is not a valid PKCS8 file
	 */
	public static KeyPair toKeyPair(final File file, char[] password) throws IOException {
		return toKeyPair(new FileInputStream(file), password);
	}


	/**
	 * Takes a PEM-encoded non-password-protected PKCS8 key-containing Reader and returns the KeyPair within. Only the first keypair is considered
	 * 
	 * @return
	 * @throws IOException if the stream is not a valid PKCS8 wrapped keypair
	 */
	public static KeyPair toKeyPair(final byte[] bytes) throws IOException {
		return toKeyPair(new ByteArrayInputStream(bytes));
	}


	/**
	 * Takes a PEM-encoded non-password-protected PKCS8 key-containing InputStream and returns the KeyPair within. Only the first keypair is considered
	 * 
	 * @return
	 * @throws IOException if the stream is not a valid PKCS8 wrapped keypair
	 */
	public static KeyPair toKeyPair(final InputStream is) throws IOException {
		return toKeyPair(is, null);
	}


	public static void toWriter(final KeyPair keypair, final Writer w) throws IOException {
		try {
			PEMWriter writer = new PEMWriter(w);
			writer.writeObject(keypair);
			writer.close();
		}
		finally {
			w.close();
		}
	}


	public static void toFile(final KeyPair keypair, final File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			PEMWriter writer = new PEMWriter(fw);
			writer.writeObject(keypair);
			writer.close();
		}
		finally {
			fw.close();
		}
	}


	public static void toFile(final PublicKey key, final File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			PEMWriter writer = new PEMWriter(fw);
			writer.writeObject(key);
			writer.close();
		}
		finally {
			fw.close();
		}
	}


	public static void toFile(final PrivateKey key, final File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			PEMWriter writer = new PEMWriter(fw);
			writer.writeObject(key);
			writer.close();
		}
		finally {
			fw.close();
		}
	}


	public static void toStream(final KeyPair keypair, OutputStream os) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(os);
		try {
			toWriter(keypair, w);
		}
		finally {
			w.close();
		}
	}


	public static byte[] toBytes(KeyPair keypair) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

		toStream(keypair, bos);

		return bos.toByteArray();
	}


	/**
	 * Takes a PEM-encoded PKCS8 key-containing InputStream and returns the KeyPair within. Only the first keypair is considered
	 * 
	 * @return
	 * @throws IOException if the stream is not a valid PKCS8 wrapped keypair
	 */
	public static KeyPair toKeyPair(final InputStream is, final char[] password) throws IOException {
		PasswordFinder passwordFinder = password != null ? new StaticPasswordFinder(password) : null;

		KeyPair kp = null;
		try {
			// read the stream as a PEM encoded
			try {

				final PEMReader pem = new PEMReader(new InputStreamReader(is), passwordFinder);
				try {
					// Skip over entries in the file which are not KeyPairs
					do {
						final Object o = pem.readObject();

						if (o == null)
							break; // at end of file
						else if (o instanceof KeyPair)
							kp = (KeyPair) o;
					} while (kp == null);
				}
				finally {
					pem.close();
				}
			}
			catch (Exception e) {
				throw new IOException("Error reading PEM stream: " + e.getMessage(), e);
			}
		}
		finally {
			is.close();
		}

		// Cast the return to a KeyPair (or, if there is no [valid] return, throw an exception)
		if (kp != null)
			return kp;
		else
			throw new IOException("Stream " + is + " did not contain a PKCS8 KeyPair");
	}


	public static X509Certificate[] toCertificateList(final InputStream is) throws IOException {
		PasswordFinder passwordFinder = null;

		List<X509Certificate> certs = new ArrayList<X509Certificate>();

		Object o = null;
		try {
			// read the stream as a PEM encoded
			try {

				final PEMReader pem = new PEMReader(new InputStreamReader(is), passwordFinder);
				try {
					// Skip over entries in the file which are not KeyPairs
					do {
						o = pem.readObject();

						if (o != null && o instanceof X509Certificate) {
							certs.add((X509Certificate) o);
						}

					} while (o != null);
				}
				finally {
					pem.close();
				}
			}
			catch (Exception e) {
				throw new IOException("Error reading PEM stream: " + e.getMessage(), e);
			}
		}
		finally {
			is.close();
		}

		return certs.toArray(new X509Certificate[certs.size()]);
	}
}