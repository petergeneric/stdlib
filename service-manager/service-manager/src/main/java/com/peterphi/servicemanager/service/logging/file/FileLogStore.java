package com.peterphi.servicemanager.service.logging.file;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import com.peterphi.servicemanager.service.logging.LogStore;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Writes log lines to disk, one file per day (a file contains log entries received on that day, not necessarily generated that
 * day)
 */
public class FileLogStore implements LogStore
{
	private static final Logger log = Logger.getLogger(FileLogStore.class);

	@Inject
	@Named("logging-store.path")
	private File storePath;

	private FileWriter currentFile;
	private long timestampForNextFile = 0;


	@Override
	public synchronized void store(List<LogLineTableEntity> lines) throws RuntimeException
	{
		if (System.currentTimeMillis() > timestampForNextFile)
		{
			// Close the existing file
			IOUtils.closeQuietly(currentFile);
			currentFile = null;

			try
			{
				FileWriter fw = new FileWriter(new File(storePath, LocalDate.now().toString() + ".log"), true);

				currentFile = fw;
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error opening new log file! " + e.getMessage(), e);
			}

			// Get the timestamp for the start of tomorrow
			timestampForNextFile = LocalDate.now().plusDays(1).toDateTimeAtStartOfDay().getMillis();
		}

		// Now append all the logs
		for (LogLineTableEntity line : lines)
		{
			try
			{
				currentFile.append(format(line));
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error appending log line " + line + " to log file! " + e.getMessage(), e);
			}
		}

		try
		{
			currentFile.flush();
		}
		catch (IOException e)
		{
			// ignore
		}
	}


	@Override
	public boolean isSearchSupported()
	{
		return false;
	}


	@Override
	public List<LogLineTableEntity> search(final DateTime from,
	                                       final DateTime to,
	                                       final String filter) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("FileLogStore cannot be searched!");
	}


	CharSequence format(final LogLineTableEntity line)
	{
		StringBuilder sb = new StringBuilder(64);

		sb.append(line.getDateTimeWhen().toString())
		  .append(' ')
		  .append(level(line.getLevel()))
		  .append(' ')
		  .append(line.getEndpoint())
		  .append(' ')
		  .append(line.getMessage());
		if (line.getException() != null)
		{
			sb.append("\n").append("exception:" + line.getExceptionId()).append(' ').append(line.getException());
		}

		return sb;
	}


	private String level(final int level)
	{
		switch (level)
		{
			case 2:
				return "TRACE";
			case 3:
				return "DEBUG";
			case 4:
				return "INFO";
			case 5:
				return "WARN";
			case 6:
				return "ERROR";
			case 7:
				return "FATAL";
			default:
				return "UNKWN";
		}
	}
}
