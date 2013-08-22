package com.mediasmiths.std.io.transfer.driver;

import java.io.*;
import java.net.*;

import com.mediasmiths.std.NotImplementedException;
import com.mediasmiths.std.io.transfer.*;

/**
 * 
 * <p>
 * Title: HTTP Transport Driver
 * </p>
 * 
 * <p>
 * Description: Thread-safe HTTP downloading driver for RemoteFS
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 */
public class HttpDriver implements IRemoteFS {
	private static final int CHUNKSIZE = 8192; // The buffer size

	private String scheme;
	private String userInfo;
	private String server;
	private int port;
	private String basePath;


	@Override
	public boolean _connect(URI remote) {
		scheme = remote.getScheme();
		server = remote.getHost();
		port = remote.getPort();
		userInfo = remote.getUserInfo();
		basePath = remote.getPath();

		return false;
	}


	private URI getURI(String remoteFile) {
		try {
			URI i;

			// If the remoteFile doesn't start with /, the path is relative to the path we connect()ed to
			if (!remoteFile.startsWith("/") && basePath != null && basePath.length() > 0) {
				remoteFile = RemoteFS.pathConcat(basePath, remoteFile);
			}

			i = new URI(scheme, userInfo, server, port, basePath, null, null);

			return i;
		}
		catch (URISyntaxException e) {
			return null;
		}
	}


	@Override
	public boolean close() {
		// No action necessary
		return true;
	}


	@Override
	public boolean upload(File localFile, String remoteFile) {
		throw new NotImplementedException("HttpDriver.upload has not been implemented yet");
	}


	// The remote file is expressed as an absolute path:
	@Override
	public boolean download(String remoteFile, File localFile) {
		OutputStream os = null;
		InputStream is = null;

		boolean success = false;
		try {
			URI uri = getURI(remoteFile);

			URLConnection conn = uri.toURL().openConnection();
			os = new BufferedOutputStream(new FileOutputStream(localFile));
			is = new BufferedInputStream(conn.getInputStream());

			// Set up a buffer for CHUNKSIZE bytes (default 8KB)
			byte[] buff = new byte[CHUNKSIZE];

			// Keep copying until there's nothing left to read
			for (int readBytes = 0; readBytes != -1; readBytes = is.read(buff)) {
				os.write(buff, 0, readBytes);
			}

			success = true;
		}
		catch (Exception e) {
			// do nothing. fail.
			success = false;
		}
		finally {
			try { // clean up the Input/Output streams
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			}
			catch (IOException e) {
				// ignore
			}

			if (!success) {
				if (localFile.exists()) {
					localFile.delete();
				}
			}
		}
		return success;
	}


	@Override
	public boolean delete(String remoteFile) {
		throw new NotImplementedException("HttpDriver.delete has not been implemented yet");
	}


	@Override
	public boolean exists(String remoteFile) {
		throw new NotImplementedException("HttpDriver.exists has not been implemented yet");
	}


	@Override
	public boolean isFile(String remoteFile) {
		throw new NotImplementedException("HttpDriver.isFile has not been implemented yet");
	}


	@Override
	public boolean isDirectory(String remotefile) {
		throw new NotImplementedException("HttpDriver.isDirectory has not been implemented yet");
	}


	@Override
	public boolean mkdir(String remoteDirectory) {
		throw new NotImplementedException("HttpDriver.mkdir has not been implemented yet");
	}


	@Override
	public boolean rmdir(String remoteDirectory) {
		throw new NotImplementedException("HttpDriver.rmdir has not been implemented yet");
	}
}
