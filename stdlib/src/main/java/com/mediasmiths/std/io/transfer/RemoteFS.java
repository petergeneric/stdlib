package com.mediasmiths.std.io.transfer;

import java.net.URI;
import java.util.Hashtable;

import com.mediasmiths.std.io.transfer.driver.*;

import org.apache.log4j.Logger;

/**
 * <p>
 * Title: Remote Filesystem Abstraction
 * </p>
 * 
 * <p>
 * Description: Defines a remote filesystem
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * @version $Revision$
 */
public final class RemoteFS {
	private static final Logger log = Logger.getLogger(RemoteFS.class);

	private static final Hashtable<String, Class<? extends IRemoteFS>> drivers = new Hashtable<String, Class<? extends IRemoteFS>>(
			10);

	static {
		register("sftp", SftpDriver.class);
		register("http", HttpDriver.class);
	}


	public static IRemoteFS connect(URI remote) {

		String scheme = remote.getScheme();
		if (drivers.containsKey(scheme)) {
			try {
				Class<? extends IRemoteFS> c = drivers.get(scheme);

				IRemoteFS fs = c.newInstance();
				fs._connect(remote);

				return fs;
			}
			catch (IllegalAccessException e) {
				log.error("[RemoteFS] {connect} Error connecting: " + e.getMessage(), e);
				return null;
			}
			catch (InstantiationException e) {
				log.error("[RemoteFS] {connect} Error connecting: " + e.getMessage(), e);
				return null;
			}
		}
		else {
			log.info("[RemoteFS] {connect} Unsupported scheme: " + scheme);
			return null;
		}
	}


	public static boolean register(String scheme, Class<? extends IRemoteFS> driver) {
		return register(scheme, driver, false);
	}


	public static boolean register(String scheme, Class<? extends IRemoteFS> driver, boolean overwrite) {
		// ensure this Class is a subclass of RemoteFS:

		if (overwrite || !drivers.containsKey(scheme)) {
			if (drivers.containsKey(scheme)) { // Remove the scheme if it's already there:
				drivers.remove(scheme);
			}

			drivers.put(scheme, driver);
			return true;
		}
		else {
			return false;
		}
	}


	public static String uriToPath(URI i) {
		return i.getPath();
	}


	/**
	 * Intelligently concatenates the two path components (assuming base is a directory) Assumes both paths are UNIX paths or URLs
	 * 
	 * @param base String The base directory
	 * @param path String The extension from the base
	 * @return String The base and path concatenated pathwise
	 */
	public static String pathConcat(String base, String path) {
		return pathConcat(base, path, "/");
	}


	/**
	 * Intelligently concatenates the two path components (assuming base is a directory). Assumes both paths use the windows \ separator
	 * 
	 * @param base String The base directory
	 * @param path String The extension from the base
	 * @return String The base and path concatenated pathwise
	 */
	public static String pathConcatWin32(String base, String path) {
		return pathConcat(base, path, "\\");
	}


	public static String pathConcat(String base, String path, String pathSeparator) {
		boolean baseEndSlash = base.endsWith(pathSeparator);
		boolean pathStartSlash = path.startsWith(pathSeparator);

		if (baseEndSlash && pathStartSlash) { // "base/" "/path"
			return base + pathSeparator.substring(1); // Remove the 1st char from the path
		}
		else if (!baseEndSlash && !pathStartSlash) { // "base" "path"
			return base + pathSeparator + path;
		}
		else { // base/ path or base /path
			return base + path;
		}
	}


	// Prevent instantiation:
	private RemoteFS() {
	}
}
