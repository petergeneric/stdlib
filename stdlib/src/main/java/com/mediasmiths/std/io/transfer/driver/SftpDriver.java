package com.mediasmiths.std.io.transfer.driver;

import java.io.*;
import java.net.URI;
import java.security.*;
import java.security.interfaces.*;

import com.mediasmiths.std.NotImplementedException;
import com.mediasmiths.std.io.transfer.IRemoteFS;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import com.sshtools.j2ssh.*;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.configuration.*;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.rsa.SshRsaPrivateKey;

@Deprecated
public class SftpDriver implements IRemoteFS {
	private static final Logger log = Logger.getLogger(SftpDriver.class);

	private SshClient ssh;
	private SftpClient sftp;
	private boolean forcePki = false;


	static {
		if (true) {
			if (Security.getProvider("BC") == null) {
				log.info("Loading Bouncy Castle Provider");
				Security.addProvider(new BouncyCastleProvider());
				log.debug("Bouncy Castle Provider loaded");
			}
		}
	}


	/**
	 * @deprecated For transport driver system
	 */
	@Deprecated
	public SftpDriver() {
	}


	public SftpDriver(URI uri, String keyFolder, boolean forcePki) {
		this.forcePki = forcePki;
		_connect(uri, keyFolder);
	}


	public SftpDriver(URI uri, String keyFolder) {
		this(uri, keyFolder, false);
	}


	public SftpDriver(URI uri) {
		this(uri, null);
	}


	private SshClient connect(URI uri, String keyFolder) {
		if (keyFolder == null)
			keyFolder = "";


		log.info("[SftpDriver] {connect} Connecting : " + uri.toString());

		String[] userAndPass = uri.getUserInfo().split(":");
		try {
			ConfigurationLoader.initialize(false);
		}
		catch (ConfigurationException e) {
			log.error("[SftpDriver] {connect} Error initialising connection: " + e.getMessage(), e);
		}
		String hostname = uri.getHost();
		String username = userAndPass[0];
		String password = userAndPass[1];

		// Connect
		SshClient ssh = new SshClient();
		log.info("[SftpTransport] {connect} Connecting to ssh server " + hostname);
		try {
			ssh.connect(hostname, new IgnoreHostKeyVerification());
		}
		catch (IOException e) {
			log.error("[SftpDriver] {connect} Error connecting to SSH site: " + e.getMessage(), e);
			return null;
		}

		SshAuthenticationClient authClient;
		try {
			if (forcePki || username.startsWith("pki-")) {
				log.info("[SftpDriver] {connect} Sending 'certificate' authentication...");

				PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();

				// Strip the "should use PKI" header
				username = username.replace("pki-", "");
				pk.setUsername(username);

				// Retrieve the key from the keystore
				PEMReader pemPrivate = new PEMReader(new FileReader(keyFolder + password + ".pem"));
				try {
					KeyPair kp = (KeyPair) pemPrivate.readObject();
	
					log.info("[SftpDriver] {connect} Certificate auth: user=" + username + " file=" + keyFolder + password + ".pem");
	
					SshRsaPrivateKey k = new SshRsaPrivateKey((RSAPrivateKey) kp.getPrivate(), (RSAPublicKey) kp
							.getPublic());
					pk.setKey(k);
	
					authClient = pk;
				}
				finally {
					IOUtils.closeQuietly(pemPrivate);
				}
			}
			else {
				log.info("[SftpDriver] {connect} Sending 'password' authentication...");
				// Use password authentication:
				PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
				pwd.setUsername(username);
				pwd.setPassword(password);

				authClient = pwd;
			}
		}
		catch (IOException e) {
			log.info("[SftpDriver] {connect} Error creating authentication object: " + e.getMessage(), e);
			throw new Error("Error creating authentication object: " + e.getMessage(), e);
		}



		// Authenticate
		int result;
		try {
			result = ssh.authenticate(authClient);
		}
		catch (IOException e) {
			result = AuthenticationProtocolState.FAILED;
			log.error("[SftpDriver] {connect} Error during authentication: " + e.getMessage(), e);
		}

		log.info("[SftpDriver] {connect} ssh server responded with code " + result);

		// If authenticated, start sftp
		if (result == AuthenticationProtocolState.COMPLETE) {
			log.info("[SftpDriver] {connect} Connection success!");
			return ssh;
		}
		else if (result == AuthenticationProtocolState.FAILED) {
			log.error("[SftpDriver] {connect} Authentication failed.");

			return null;
		}
		else if (result == AuthenticationProtocolState.CANCELLED) {
			log.error("[SftpDriver] {connect} Authentication cancelled.");
			return null;
		}
		else {
			log.info("[SftpDriver] {connect} Unknown connection result state " + result);
			return null;
		}
	}


	@Override
	public boolean _connect(URI remote) {
		this.ssh = connect(remote, null);

		if (this.ssh != null) {
			try {
				this.sftp = ssh.getActiveSftpClient();
			}
			catch (IOException e) {
				log.error("[SftpDriver] {_connect} Error getting SFTP client: " + e.getMessage(), e);
			}
		}

		return (this.sftp != null);
	}


	public boolean _connect(URI remote, String keyFolder) {
		this.ssh = connect(remote, keyFolder);

		if (this.ssh != null) {
			try {
				this.sftp = ssh.openSftpClient();
			}
			catch (IOException e) {
				log.error("[SftpDriver] {_connect} Error getting SFTP client: " + e.getMessage(), e);
			}
		}

		return (this.sftp != null);
	}


	/**
	 * Closes the connection to the remote server.
	 */
	@Override
	public boolean close() {
		if (this.ssh != null && ssh.isConnected()) {
			ssh.disconnect();
		}

		return true;
	}


	@Override
	public boolean upload(File localFile, String remoteFile) {
		throw new NotImplementedException("SftpDriver.upload has not been implemented yet");
	}


	// The remote file is expressed as an absolute path:
	@Override
	public boolean download(String remoteFile, File localFile) {
		try {
			log.info("[SftpDriver] {download} Get " + remoteFile + " to " + localFile);
			sftp.get(remoteFile, localFile.getPath());
		}
		catch (IOException e) {
			log.error("[SftpDriver] {download} " + e.getMessage(), e);
			return false;
		}

		return localFile.exists();
	}


	@Override
	public boolean delete(String remoteFile) {
		try {
			sftp.rm(remoteFile, true, false);
			return true;
		}
		catch (IOException e) {
			log.error("[SftpDriver] {delete} " + e.getMessage(), e);
			return false;
		}
	}

	public long getSize(String remoteFile) {
		FileAttributes attr = stat(remoteFile);

		long size = -1;
		if (attr != null)
			size = attr.getSize().longValue();
		else
			size = -1;

		log.info("[SftpDriver] {getSize} size of : " + remoteFile + " is " + size);

		return size;
	}

	private FileAttributes stat(String remoteFile) {
		try {
			return sftp.stat(remoteFile);
		}
		catch (IOException e) {
			return null;
		}
	}


	@Override
	public boolean exists(String remoteFile) {
		FileAttributes attr = stat(remoteFile);

		return (attr != null);
	}


	@Override
	public boolean isFile(String remoteFile) {
		FileAttributes attr = stat(remoteFile);

		if (attr != null) {
			return attr.isFile();
		}
		else {
			return false;
		}
	}


	@Override
	public boolean isDirectory(String remoteFile) {
		FileAttributes attr = stat(remoteFile);

		if (attr != null) {
			return attr.isDirectory();
		}
		else {
			return false;
		}

	}


	@Override
	public boolean mkdir(String remoteDirectory) {
		try {
			sftp.mkdirs(remoteDirectory);
			return true;
		}
		catch (Error e) {
			log.error("[SftpDriver] {mkdir} " + e.getMessage(), e);
			return false;
		}
	}


	@Override
	public boolean rmdir(String remoteDirectory) {
		try {
			sftp.rm(remoteDirectory, true, true);
			return true;
		}
		catch (IOException e) {
			log.error("[SftpDriver] {rmdir} " + e.getMessage(), e);
			return false;
		}
	}


	@Override
	public void finalize() {
		close();
	}
}
