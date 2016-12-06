package com.peterphi.servicemanager.hostagent.webapp.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.io.PropertyFile;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

public class TomcatService
{
	@Inject(optional = true)
	@Named("tomcat.root")
	public File tomcatRoot = new File("/opt/tomcat");


	public Map<File, Integer> getTomcatsAndPorts()
	{
		final Map<File, Integer> map = new HashMap<>();

		for (File domain : tomcatRoot.listFiles(new TomcatDomainFilenameFilter()))
		{
			try
			{
				PropertyFile props = new PropertyFile(new File(domain, "tomcat.properties"));

				final int port = props.get("tomcat.port", -1);
				final String path = props.get("tomcat.webapp.folder");

				final File webappFolder = new File(path);

				if (port <= 0)
					throw new IllegalArgumentException("No port specified!");
				else if (!webappFolder.exists())
					throw new IllegalArgumentException("No valid webapp folder specified: does not exist: " + webappFolder);

				map.put(webappFolder, port);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Error reading tomcat.properties for " + domain, e);
			}
		}

		return map;
	}


	public static class TomcatDomainFilenameFilter implements FileFilter
	{

		@Override
		public boolean accept(final File pathname)
		{
			if (!pathname.isDirectory())
				return false;

			return new File(pathname, "tomcat.properties").exists();
		}
	}
}
