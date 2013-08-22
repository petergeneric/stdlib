package com.mediasmiths.std.crypto.keystore;

import java.security.KeyPair;
import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;
import com.mediasmiths.std.crypto.digest.KeyFingerprinting;

/**
 * A set of KeyPairs, indexed and retrieved by their private key's fingerprint (the hex-encoded SHA1 digest of the DER-encoded key)<br />
 * Keys are persisted to a directory (with the contents of that directory exclusively managed by this object)
 */
public class FingerprintKeyStorage {
	private static transient final Logger log = Logger.getLogger(FingerprintKeyStorage.class);

	private static final String keyFileExtension = ".pkcs8";

	private final Map<String, KeyPair> _storage = new HashMap<String, KeyPair>();
	private final File directory;


	public FingerprintKeyStorage(final File directory) {
		this.directory = directory;

		if (!directory.exists())
			directory.mkdirs();

		scanAll();
	}


	protected void scanAll() {
		_storage.clear();

		final File[] files = directory.listFiles();

		if (files == null) {
			log.warn("{scanAll} null result from listFiles on " + directory.getAbsolutePath());
		}

		if (files != null)
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith(keyFileExtension)) {
					try {
						final KeyPair keypair = PKCS8Util.toKeyPair(file);

						if (keypair != null)
							add(keypair);
					}
					catch (IOException e) {
						log.warn("{scanAll} Error loading key from " + file.getAbsolutePath() + ". Error: " + e.getMessage(), e);
					}
				}
			}
	}


	public KeyPair get(final String fingerprint) {
		return _storage.get(normalise(fingerprint));
	}


	/**
	 * Returns a read-only set containing a list of all the keypairs currently in storage
	 * 
	 * @return
	 */
	public Set<KeyPair> getAll() {
		final Collection<KeyPair> values = _storage.values();

		final Set<KeyPair> copy = new HashSet<KeyPair>(values);

		return Collections.unmodifiableSet(copy);
	}


	public boolean contains(final String fingerprint) {
		return _storage.containsKey(normalise(fingerprint));
	}


	public void add(final KeyPair keypair) {
		final String fingerprint = fingerprint(keypair);
		if (log.isInfoEnabled())
			log.info("{add} Adding keypair with fingerprint: " + fingerprint);

		writeFile(fingerprint, keypair);
		_storage.put(fingerprint, keypair);
	}


	public void remove(final KeyPair keypair) {
		final String fingerprint = fingerprint(keypair);

		deleteFile(fingerprint, keypair);
		_storage.remove(fingerprint);
	}


	public void clear() {
		for (KeyPair keypair : getAll())
			remove(keypair);
	}


	public int size() {
		return _storage.size();
	}


	// file handling methods

	protected void deleteFile(final String fingerprint, final KeyPair keypair) {
		final File file = getFile(fingerprint, keypair);

		file.delete();
	}


	protected File getFile(final String fingerprint, final KeyPair keypair) {
		final String filename = fingerprint + keyFileExtension;
		final File file = new File(directory, filename);

		return file;
	}


	protected void writeFile(final String fingerprint, final KeyPair keypair) {
		final File file = getFile(fingerprint, keypair);

		try {
			PKCS8Util.toFile(keypair, file);
		}
		catch (IOException e) {
			final String absolutePath = file.getAbsolutePath();
			file.delete();

			throw new RuntimeException("Error writing keypair to file " + absolutePath + ": " + e.getMessage());
		}
	}


	// convenience methods

	public boolean contains(KeyPair keypair) {
		return contains(fingerprint(keypair));
	}


	public void addAll(final Iterable<KeyPair> keypairs) {
		for (KeyPair keypair : keypairs)
			add(keypair);
	}


	public void remove(final String fingerprint) {
		final KeyPair keypair = get(fingerprint);

		remove(keypair);
	}


	public void removeAll(final Iterable<KeyPair> keypairs) {
		for (KeyPair keypair : keypairs)
			remove(keypair);
	}


	// static utility methods

	/**
	 * Creates a (normalised) fingerprint of a keypair
	 */
	protected static String fingerprint(final KeyPair keypair) {
		return normalise(KeyFingerprinting.fingerprint(keypair));
	}


	/**
	 * Normalises an existing fingerprint
	 * 
	 * @param fingerprint
	 * @return
	 */
	protected static String normalise(final String fingerprint) {
		return fingerprint.toLowerCase().replace(":", "");
	}
}
