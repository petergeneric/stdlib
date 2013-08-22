package com.mediasmiths.std.crypto.keystore;

import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.openssl.*;

public class DirectoryKeystore {
	private static final Logger log = Logger.getLogger(DirectoryKeystore.class);

	static {
		if (Security.getProvider("BC") == null) {
			log.debug("[DirectoryKeystore] {static} Initialising BC Provider");
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private final File _keyFolder;
	private final File _publicFolder;
	private final File _privateFolder;


	public DirectoryKeystore(File directory) {
		log.info("[DirectoryKeystore] {ctor} Initialising from " + directory.toString());
		_keyFolder = directory;
		_publicFolder = new File(_keyFolder, "public");
		_privateFolder = new File(_keyFolder, "private");
	}


	public PublicKey[] loadPublicKeys() {
		PublicKey[] buffer;

		File[] files = _publicFolder.listFiles();

		buffer = new PublicKey[files.length];
		for (int i = 0; i < files.length; i++) {
			buffer[i] = getPublicKey(files[i]);
		}

		return buffer;
	}


	public PrivateKey getPrivateKey(String name) {
		File keyFile = new File(_privateFolder, name);

		return getPrivateKey(keyFile);
	}


	public PublicKey getPublicKey(String name) {
		File keyFile = new File(_publicFolder, name);

		return getPublicKey(keyFile);
	}


	public boolean hasKeyPair(String name) {
		try {
			return (getKeypair(name) != null);
		}
		catch (Throwable t) {
			return false;
		}
	}


	public KeyPair getKeypair(String name) {
		PrivateKey kPrivate;
		PublicKey kPublic;

		try {
			log.debug("[DirectoryKeystore] {getKeypair} Getting private key");
			kPrivate = getPrivateKey(name);
		}
		catch (Exception e) {
			log.error("[DirectoryKeystore] {getKeypair} Error retrieving private key: " + e.getMessage(), e);
			kPrivate = null;
		}

		try {
			log.debug("[DirectoryKeystore] {getKeypair} Getting public key");
			kPublic = getPublicKey(new File(_privateFolder, name));
		}
		catch (Exception e) {
			kPublic = null;
		}

		if (kPrivate != null && kPublic != null) {
			return new KeyPair(kPublic, kPrivate);
		}
		else if (kPrivate == null && kPublic == null) {
			log.info("[DirectoryKeystore] {getKeypair} No public or private key '" + name + "' found");
			// This key doesn't exist
			return null;
		}
		else if (kPrivate == null) {
			// There's no private key in the private folder. Fail.
			log.warn("[DocumentKeystore] {getKeypair} private:" + name + " not in private keystore.");
			return null;
		}
		else if (kPublic == null) {
			// no public key
			log.warn("[DocumentKeystore] {getKeypair} public:" + name + " not in private keystore.");
			return null;
		}

		// unreachable
		return null;
	}


	public boolean setPrivateKey(String name, PrivateKey pk) {
		return setKey(new File(_privateFolder, name), pk);
	}


	public boolean setPublicKey(String name, PublicKey kpub) {
		return setKey(new File(_publicFolder, name), kpub);
	}


	public boolean setPublicKey(String name, X509Certificate cert) {
		return setKey(new File(_publicFolder, name), cert);
	}


	public boolean setKeypair(String name, KeyPair kp) {
		return setKey(new File(_privateFolder, name), kp);
	}


	private static boolean setKey(File keyFile, Object keyObject) {
		try {
			PEMWriter w = new PEMWriter(new FileWriter(keyFile));
			w.writeObject(keyObject);
			w.close();

			return true;
		}
		catch (Throwable t) {
			log.error("[DirectoryKeystore] {setKey} Error writing key: " + t.getMessage(), t);
			return false;
		}
	}


	public static PrivateKey getPrivateKey(File keyFile) {
		try {
			if (keyFile.exists()) {
				log.debug("[DirectoryKeystore] {getPrivateKey} Private keyfile exists.");
				PEMReader r = new PEMReader(new FileReader(keyFile), null, "BC");
				try {
					log.debug("[DirectoryKeystore] {getPrivateKey} PEM file loaded.");
	
					Object o;
					try {
						o = r.readObject();
					}
					catch (Throwable e) {
						log.error(
								"[DirectoryKeystore] {getPrivateKey} Error reading keyfile (it might be encrypted?). Error: "
										+ e.getMessage(), e);
						return null;
					}
	
					// A public/private keypair
					if (o instanceof KeyPair) {
						KeyPair kp = (KeyPair) o;
	
						return kp.getPrivate();
					}
					else if (o instanceof PrivateKey) {
						return (PrivateKey) o;
					}
					else {
						log.error("[DirectoryKeystore] Unknown key format: " + o.getClass());
						return null;
					}
				}
				finally {
					IOUtils.closeQuietly(r);
				}
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			log.error("[DirectoryKeystore] {getPrivateKey} Error getting key: " + e.getMessage(), e);
			return null;
		}
	}


	public static PublicKey getPublicKey(File keyFile) {
		try {
			if (keyFile.exists()) {
				PEMReader r = new PEMReader(new FileReader(keyFile), null, "BC");
				try {
					Object o;
					try {
						o = r.readObject();
					}
					catch (IOException e) {
						log.error(
								"[DirectoryKeystore] {getPrivateKey} Error reading keyfile (it might be encrypted?). Error: "
										+ e.getMessage(), e);
						return null;
					}
	
					// An X509 certificate
					if (o instanceof X509CertificateObject) {
						try {
							X509CertificateObject cert = (X509CertificateObject) o;
							cert.checkValidity();
							return cert.getPublicKey();
						}
						catch (java.security.cert.CertificateNotYetValidException e) {
							log.info("[DirectoryKeystore] {getPublicKey} Key not yet valid");
							return null;
						}
						catch (java.security.cert.CertificateExpiredException e) {
							log.info("[DirectoryKeystore] {getPublicKey} Key expired");
							return null;
						}
					}
					else if (o instanceof PublicKey) {
						return (PublicKey) o;
					}
					// A public/private keypair
					else if (o instanceof KeyPair) {
						KeyPair kp = (KeyPair) o;
	
						return kp.getPublic();
					}
					else {
						log.error("[DirectoryKeystore] Unknown key format: " + o.getClass());
						return null;
					}
				}
				finally {
					IOUtils.closeQuietly(r);
				}
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			log.error("[DirectoryKeystore] {getPublicKey} Error getting key: " + e.getMessage(), e);
			return null;
		}
	}
}
