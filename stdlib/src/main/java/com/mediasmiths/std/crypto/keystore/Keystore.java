package com.mediasmiths.std.crypto.keystore;

import java.io.*;

import com.mediasmiths.std.config.annotation.Optional;

/**
 * <p>
 * Title: Java Keystore Reference
 * </p>
 * 
 * <p>
 * Description: A (type,file,password) tuple to describe a keystore (eg. a JKS or P12)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 */
public class Keystore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public File file;

	@Optional
	public String password = "";

	@Optional
	public String type = "JKS";


	public Keystore() {
	}


	public Keystore(String type, File file, String password) {
		this.type = type;
		this.file = file;
		this.password = password;
	}


	public String getType() {
		return type;
	}


	public File getFile() {
		return file;
	}


	public String getPassword() {
		return password;
	}


	public void setType(String type) {
		this.type = type;
	}


	public void setFile(File file) {
		this.file = file;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Simple, safe method to determine if this keystore file exists
	 * 
	 * @return boolean True if the file has been specified & exists
	 */
	public boolean exists() {
		if (file != null) {
			return file.exists();
		}
		else {
			return false;
		}
	}


	@Override
	public String toString() {
		return "keystore-" + type + ":" + file.getPath();
	}


	public void readFromSystem(boolean truststore) {
		String propName = (truststore ? "javax.net.ssl.trustStore" : "javax.net.ssl.keyStore");

		String tsName = System.getProperty(propName);
		if (tsName != null && tsName.length() > 0) {
			File tsFile = new File(tsName);
			setFile(tsFile);
		}

		setType(System.getProperty(propName + "Type"));
		setPassword(System.getProperty(propName + "Password"));
	}


	public void writeToSystem(boolean truststore) {
		String propName = (truststore ? "javax.net.ssl.trustStore" : "javax.net.ssl.keyStore");

		if (getFile() != null)
			System.setProperty(propName, getFile().getPath());

		if (getType() != null)
			System.setProperty(propName + "Type", getType());

		if (getPassword() != null)
			System.setProperty(propName + "Password", getPassword());
	}
}
