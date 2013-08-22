package com.mediasmiths.std.io.transfer;

import java.io.File;
import java.net.URI;

public interface IRemoteFS {
	/**
	 * Takes any necessary actions to connect to the remote server. This should ONLY be called by the RemoteFS factory, NOT a consumer
	 * 
	 * @param remote The remote server to connect to
	 * @return True if the connection worked, otherwise false. No exceptions should be produced
	 */
	boolean _connect(URI remote);


	/**
	 * Closes the connection to the remote server. This MUST be called to guarantee resources are freed upon completion
	 * 
	 * @return The success of the operation (or true if the connection is already closed)
	 */
	boolean close();


	boolean upload(File localFile, String remoteFile);


	boolean download(String remoteFile, File localFile);


	boolean delete(String remoteFile);


	boolean exists(String remoteFile);


	boolean isFile(String remoteFile);


	boolean isDirectory(String remoteFile);


	boolean mkdir(String remoteDirectory);


	boolean rmdir(String remoteDirectory);
}
