package com.peterphi.carbon.type.mutable;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

public class CarbonFTPSettings extends XMLWrapper
{

	public final static String CARBON_FTP_SETTINGS_ELEMENT_NAME = "FTPSettings";

	public static final String FTP_PASSWORD_ATTRIBUTE = "FTPPassword";
	public static final String FTP_USERNAME_ATTRIBUTE = "FTPUser";
	public static final String FTP_SERVER_ATTRIBUTE = "FTPServer";

	public CarbonFTPSettings(Element element)
	{
		super(element);
	}

	public void setPassword(String password)
	{
		setAttribute(FTP_PASSWORD_ATTRIBUTE, password);
	}

	public void setUsername(String user)
	{
		setAttribute(FTP_USERNAME_ATTRIBUTE, user);
	}

	public void setServer(String server)
	{
		setAttribute(FTP_SERVER_ATTRIBUTE, server);
	}

}
