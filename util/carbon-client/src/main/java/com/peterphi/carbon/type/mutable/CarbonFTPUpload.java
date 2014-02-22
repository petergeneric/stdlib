package com.peterphi.carbon.type.mutable;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

public class CarbonFTPUpload extends XMLWrapper
{

	public static final String REMOTE_FTP_FILE_ATTRIBUTE = "RemoteFTPFile";
	public static final String REMOTE_FTP_FOLDER_ATTRIBUTE = "RemoteFTPFolder";

	public static String FTP_UPLOAD_ELEMENT_NAME_PREFIX = "FTPUpload_";

	public CarbonFTPUpload(Element element)
	{
		super(element);
	}

	public void setRemoteFTPFile(String filename)
	{
		setAttribute(REMOTE_FTP_FILE_ATTRIBUTE, filename);
	}

	public void setRemoteFTPFolder(String folder)
	{
		setAttribute(REMOTE_FTP_FOLDER_ATTRIBUTE, folder);
	}

	public void setFTPPassword(String password)
	{
		CarbonFTPSettings settings = getOrCreateSettingsElement();
		settings.setPassword(password);
	}

	public void setFTPUsername(String user)
	{
		CarbonFTPSettings settings = getOrCreateSettingsElement();
		settings.setUsername(user);
	}

	public void setFTPServer(String server)
	{
		CarbonFTPSettings settings = getOrCreateSettingsElement();
		settings.setServer(server);
	}

	private CarbonFTPSettings getOrCreateSettingsElement()
	{

		if (getSettingsElement() == null)
		{
			element.addContent(new Element(CarbonFTPSettings.CARBON_FTP_SETTINGS_ELEMENT_NAME));
		}

		return getSettingsElement();
	}

	private CarbonFTPSettings getSettingsElement()
	{
		return new CarbonFTPSettings(element.getChild(CarbonFTPSettings.CARBON_FTP_SETTINGS_ELEMENT_NAME));
	}

}
