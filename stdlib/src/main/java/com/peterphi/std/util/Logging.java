package com.peterphi.std.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Automates the configuration of
 */
public final class Logging
{
	public static boolean WATCH = true;
	private static File currentLogFile = null;


	private Logging()
	{
	}


	/**
	 * Configures the logging environment to use the first available config file in the list, printing an error if none of the
	 * files
	 * are suitable
	 *
	 * @param files
	 */
	public static void configureFiles(Iterable<File> files)
	{
		for (File file : files)
		{
			if (file != null && file.exists() && file.canRead())
			{
				setup(file);
				return;
			}
		}

		System.out.println("(No suitable log config file found)");
	}


	private static synchronized void setup(final File logFile)
	{
		if (currentLogFile != null && currentLogFile != logFile)
		{
			final Logger log = Logger.getLogger(Logging.class);

			log.warn("[Logging] {setup} Logging is being reconfigured to use: " + logFile.getAbsolutePath());
		}

		LogManager.resetConfiguration(); // Erase the existing configuration
		PropertyConfigurator.configureAndWatch(logFile.getAbsolutePath());

		currentLogFile = logFile;
	}


	public static void configure(File... files)
	{
		if (files == null || files.length == 0 || (files.length == 1 && files[0] == null))
			return;
		else
			configureFiles(Arrays.asList(files));
	}


	public static void configure(String... fileNames)
	{
		if (fileNames == null || fileNames.length == 0 || (fileNames.length == 1 && fileNames[0] == null))
			return;
		else
			configureNames(Arrays.asList(fileNames));
	}


	public static void configureNames(Iterable<String> fileNames)
	{
		if (fileNames == null)
			return;

		List<File> files = new ArrayList<File>();

		for (String fileName : fileNames)
		{
			if (fileName != null)
			{
				files.add(new File(fileName));
			}
		}

		configureFiles(files);
	}
}
