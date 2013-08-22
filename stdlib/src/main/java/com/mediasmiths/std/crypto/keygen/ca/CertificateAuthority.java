package com.mediasmiths.std.crypto.keygen.ca;

import java.security.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;

import com.mediasmiths.std.crypto.DNReformatter;
import com.mediasmiths.std.crypto.keygen.CaHelper;
import com.mediasmiths.std.io.FileHelper;
import com.mediasmiths.std.util.ListUtility;

public class CertificateAuthority {
	private static transient final Logger log = Logger.getLogger(CertificateAuthority.class);
	public static final boolean ENABLE_PEM_PROLOGUE = true;

	public final static Provider PROVIDER;

	static {
		if (Security.getProvider("BC") == null) {
			log.info("[CaHelper] Loading Bouncy Castle Provider");
			PROVIDER = new BouncyCastleProvider();
			Security.addProvider(PROVIDER);
			log.debug("[CaHelper] Bouncy Castle Provider loaded");
		}
		else {
			PROVIDER = Security.getProvider("BC");
		}
	}

	private final File p12;

	private final File pemCert;
	private final char[] capass;

	private X509Certificate cacert;
	private PrivateKey caSigningKey;


	public CertificateAuthority(
			final File p12,
			final char[] pass,
			final File pemCert,
			final BigInteger defaultSerial,
			final String subject) throws Exception {
		this.p12 = p12;
		this.capass = Arrays.copyOf(pass, pass.length);
		this.pemCert = pemCert;

		if (p12.exists() && pemCert.exists())
			loadCA();
		else
			generateCA(subject, defaultSerial);
	}


	public X509Certificate getCACertificate() {
		return cacert;
	}


	protected void loadCA() throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12", PROVIDER);

		ks.load(new FileInputStream(p12), capass);

		for (String alias : ListUtility.iterate(ks.aliases())) {
			if (alias == null)
				continue;

			boolean hasKey = ks.isKeyEntry(alias);
			// boolean hasCert = ks.isCertificateEntry(alias);

			if (hasKey) {
				this.cacert = (X509Certificate) ks.getCertificate(alias);
				this.caSigningKey = (PrivateKey) ks.getKey(alias, new char[] {});
			}
		}

		init();

		save(); // Overwrite the files
	}


	public void saveHash(File f) throws IOException {
		FileHelper.write(f, this.getHash());
	}


	protected void saveP12(File p12) throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12", PROVIDER);
		ks.load(null);
		// ks.setCertificateEntry("ca", cacert);
		ks.setKeyEntry("ca", caSigningKey, new char[] {}, new java.security.cert.Certificate[] { cacert });

		ks.store(new FileOutputStream(p12), new char[] {});
	}


	protected void saveJKS(File jks) throws Exception {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null);
		// ks.setCertificateEntry("ca", cacert);
		ks.setKeyEntry("ca", caSigningKey, new char[] {}, new java.security.cert.Certificate[] { cacert });

		ks.store(new FileOutputStream(jks), new char[] {});
	}


	public void savePrivatePEM(File caKeyPem) throws Exception {
		PEMWriter p = new PEMWriter(new FileWriter(caKeyPem));

		p.writeObject(caSigningKey);
		p.close();
	}


	public void savePublicPEM(File pem) throws Exception {
		PEMWriter p = new PEMWriter(new FileWriter(pem));

		if (ENABLE_PEM_PROLOGUE) {
			p.write("CA Certificate: " + cacert.getSubjectDN().getName() + "\n");
			p.write("\tIssuer:" + cacert.getIssuerDN().getName() + "\n");
			p.write("\tSerial number:" + cacert.getSerialNumber().toString(16) + "\n");
			p.write("\tNot Before:" + cacert.getNotBefore() + "\n");
			p.write("\tNot After:" + cacert.getNotAfter() + "\n");
			p.write("\tOpenSSL Hash:" + this.getHash() + "\n");
			p.write("\tNot After:" + cacert.getNotAfter() + "\n");
			p.write("\tFile written: " + new Date() + "\n");
		}

		p.writeObject(cacert);
		p.close();
	}


	public void savePublicJKS(File jks) throws Exception {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null);
		ks.setCertificateEntry("ca", cacert);

		ks.store(new FileOutputStream(jks), new char[] {});
	}


	protected void save() throws Exception {
		saveP12(this.p12);
		savePublicPEM(this.pemCert);
	}


	/**
	 * Returns the OpenSSL hash for the Certificate Authority
	 * 
	 * @return
	 */
	public String getHash() {
		return CaHelper.opensslHash(cacert);
	}


	//
	protected void generateCA(String subject, BigInteger serial) throws Exception {
		// Generates a new CA certificate
		KeyPair cakeys = CaHelper.generateKeyPair(2048);

		this.caSigningKey = cakeys.getPrivate();
		cacert = CaHelper.generateCaCertificate(subject, cakeys, serial, toX509Name(subject));

		init();
		save();
	}


	// Once load() or generateCA() have been called, init sets up things
	protected void init() {
		if (this.cacert == null || this.caSigningKey == null) {
			throw new IllegalStateException("Cannot initialise CA without both the CA Cert and the CA Signing Key");
		}
	}


	public X509Name getIssuer() {
		return toX509Name(cacert.getSubjectX500Principal());
	}


	public X509Certificate issueUser(PublicKey user, String subject) throws Exception {
		return CaHelper.generateClientCertificate(user, caSigningKey, this.getIssuer(), toX509Name(subject));
	}


	public X509Certificate issueServer(PublicKey user, String subject) throws Exception {
		X509Name ca = this.getIssuer();
		X509Name usr = toX509Name(subject);

		return CaHelper.generateServerCertificate(user, caSigningKey, ca, usr);
	}


	private static X509Name toX509Name(String subject) {
		return DNReformatter.DEFAULT.reformat(new X509Name(subject));
	}


	private static X509Name toX509Name(X500Principal subject) {
		return toX509Name(subject.getName());
	}
}
