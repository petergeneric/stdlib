package com.peterphi.std.crypto.keygen.ca;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.io.*;
import org.bouncycastle.openssl.PEMWriter;

import com.peterphi.std.crypto.keygen.CaHelper;

public class IssuedCertificate {
	public KeyPair keypair;
	public X509Certificate mycert;
	public X509Certificate cacert;


	public IssuedCertificate(X509Certificate ca, KeyPair keypair, X509Certificate certificate) {
		this.cacert = ca;
		this.keypair = keypair;
		this.mycert = certificate;
	}


	public void saveP12(File p12) throws Exception {
		saveP12(p12, new char[] {});
	}


	public void saveP12(File p12, char[] pass) throws Exception {
		OutputStream os = new FileOutputStream(p12);
		saveP12(os, pass);

		os.close();
	}


	public void saveP12(OutputStream os, char[] pass) throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12", CertificateAuthority.PROVIDER);
		ks.load(null, null);

		ks.setCertificateEntry("", cacert);
		ks.setCertificateEntry("", mycert);
		ks.setKeyEntry("", keypair.getPrivate(), new char[] {}, new java.security.cert.Certificate[] { mycert, cacert });

		ks.store(os, pass);
	}


	public void saveJKS(File jks) throws Exception {
		saveJKS(new FileOutputStream(jks));
	}


	public void saveJKS(OutputStream os) throws Exception {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null);
		// ks.setCertificateEntry("me", certificate);
		// ks.setCertificateEntry("issuer", cacert);
		ks.setKeyEntry("me", keypair.getPrivate(), new char[] {}, new java.security.cert.Certificate[] { mycert, cacert });

		ks.store(os, new char[] {});
	}


	public void saveCertPEM(File pem) throws Exception {
		saveCertPEM(new FileWriter(pem));
	}


	public void saveCertPEM(Writer ww) throws Exception {
		PEMWriter w = new PEMWriter(ww);

		writePemHeader(w);

		w.writeObject(mycert);

		w.close();
	}


	public void saveKeyPEM(File pem) throws Exception {
		saveKeyPEM(new FileWriter(pem));
	}


	public void saveKeyPEM(Writer ww) throws Exception {
		PEMWriter w = new PEMWriter(ww);

		writePemHeader(w);

		w.writeObject(keypair.getPrivate());

		w.close();
	}


	private void writePemHeader(Writer w) throws IOException {
		if (CertificateAuthority.ENABLE_PEM_PROLOGUE) {
			w.write("Certificate: " + mycert.getSubjectDN().getName() + "\n");
			w.write("\tIssuer:" + mycert.getIssuerDN().getName() + "\n");
			w.write("\tSerial number:" + mycert.getSerialNumber().toString(16) + "\n");
			w.write("\tNot Before:" + mycert.getNotBefore() + "\n");
			w.write("\tNot After:" + mycert.getNotAfter() + "\n");
			w.write("\tOpenSSL Hash:" + this.getHash() + "\n");
			w.write("\tOpenSSL CA Hash:" + this.getCAHash() + "\n");
			w.write("\tNot After:" + mycert.getNotAfter() + "\n");
			w.write("\tFile written: " + new Date() + "\n");
		}
	}


	public String getHash() {
		return CaHelper.opensslHash(mycert);
	}


	public String getCAHash() {
		return CaHelper.opensslHash(cacert);
	}


	public static IssuedCertificate generateUserCert(CertificateAuthority auth, String subject, int strength) throws Exception {
		if (strength < 1024)
			strength = 1024;

		KeyPair kp = CaHelper.generateKeyPair(strength);

		X509Certificate cert = auth.issueUser(kp.getPublic(), subject);

		return new IssuedCertificate(auth.getCACertificate(), kp, cert);
	}


	public static IssuedCertificate generateServerCert(CertificateAuthority auth, String subject, int strength)
			throws Exception {
		if (strength < 1024)
			strength = 1024;

		KeyPair kp = CaHelper.generateKeyPair(strength);

		X509Certificate cert = auth.issueServer(kp.getPublic(), subject);

		return new IssuedCertificate(auth.getCACertificate(), kp, cert);
	}
}
