package com.peterphi.std.io;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * <p>
 * Title: Stream Utility Class
 * </p>
 * <p/>
 * <p>
 * Description: Does useful things with streams
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p/>
 * <p>
 * <p/>
 * </p>
 *
 * @version $Revision$
 */
public class StreamUtil
{
	private final static Logger log = Logger.getLogger(StreamUtil.class);
	public static final int STREAM_SLEEP_TIME = 5;
	public static final int CHUNKSIZE = 8192;
	public static final int MONITOR_UPDATE_INTERVAL = 10;

	/**
	 * A monitor which does nothing
	 */
	public static final DummyMonitor DUMMY_MONITOR = new DummyMonitor();

	private static final class DummyMonitor implements ICopyProgressMonitor
	{
		private DummyMonitor()
		{
		}


		@Override
		public void blocksize(int size)
		{
		}


		@Override
		public void complete()
		{
		}


		@Override
		public void failure()
		{
		}


		@Override
		public void progress(long bytes)
		{
		}


		@Override
		public void size(long bytes)
		{
		}


		@Override
		public void start()
		{
		}
	}


	// Prevent instantiation
	private StreamUtil()
	{
	}


	/**
	 * Given a Process, this function will route the flow of data through it & out again
	 *
	 * @param p
	 * @param origin
	 * @param closeInput
	 *
	 * @return
	 */
	public static InputStream routeStreamThroughProcess(final Process p, final InputStream origin, final boolean closeInput)
	{
		InputStream processSTDOUT = p.getInputStream();
		OutputStream processSTDIN = p.getOutputStream();

		// Kick off a background copy to the process
		StreamUtil.doBackgroundCopy(origin, processSTDIN, DUMMY_MONITOR, true, closeInput);

		return processSTDOUT;
	}


	/**
	 * Eats an inputstream, discarding its contents
	 *
	 * @param is
	 * 		InputStream The input stream to read to the end of
	 *
	 * @return long The size of the stream
	 */
	public static long eatInputStream(InputStream is)
	{
		try
		{
			long eaten = 0;

			try
			{
				Thread.sleep(STREAM_SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				// ignore
			}

			int avail = Math.min(is.available(), CHUNKSIZE);
			byte[] eatingArray = new byte[CHUNKSIZE];
			while (avail > 0)
			{
				is.read(eatingArray, 0, avail);
				eaten += avail;

				if (avail < CHUNKSIZE)
				{ // If the buffer wasn't full, wait a short amount of time to let it fill up
					if (STREAM_SLEEP_TIME != 0)
						try
						{
							Thread.sleep(STREAM_SLEEP_TIME);
						}
						catch (InterruptedException e)
						{
							// ignore
						}
				}
				avail = Math.min(is.available(), CHUNKSIZE);
			}

			return eaten;
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
			return -1;
		}
	}


	public static void doBackgroundCopy(InputStream isI, OutputStream osI)
	{
		doBackgroundCopy(isI, osI, DUMMY_MONITOR);
	}


	public static void doBackgroundCopy(final InputStream is, final OutputStream os, final ICopyProgressMonitor monitor)
	{
		doBackgroundCopy(is, os, monitor, true, false);
	}


	public static void doBackgroundCopy(final InputStream is,
	                                    final OutputStream os,
	                                    final ICopyProgressMonitor monitor,
	                                    final boolean closeOutput,
	                                    final boolean closeInput)
	{
		if (null == monitor)
		{
			throw new IllegalArgumentException("Must provide a monitor (try StreamUtil.DUMMY_MONITOR)");
		}

		final Thread eatThread = (new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					streamCopy(is, os, monitor);
				}
				finally
				{

					if (closeOutput || closeInput)
					{
						try
						{
							if (closeOutput)
								os.close();

							if (closeInput)
								is.close();
						}
						catch (IOException e)
						{
						}
					}
				}
			}
		});

		eatThread.setName("backgroundCopy " + is + " to " + os);
		eatThread.setDaemon(true);
		eatThread.start();
	}


	public static void streamCopy(final InputStream in, final OutputStream out)
	{
		streamCopy(in, out, DUMMY_MONITOR);
	}


	public static void streamCopy(final InputStream in, final OutputStream out, final ICopyProgressMonitor monitor)
	{
		if (null == monitor)
		{
			streamCopy(in, out, DUMMY_MONITOR);
			return;
		}

		try
		{
			monitor.start();
			monitor.blocksize(CHUNKSIZE);

			byte[] eatingArray = new byte[CHUNKSIZE];

			int loops = 0;
			long totalSize = 0;
			while (true)
			{
				int numbytes = in.read(eatingArray, 0, CHUNKSIZE);

				// Finish immediately if the input stream's closed
				if (numbytes == -1)
				{
					return;
				}
				else
					totalSize += numbytes;

				out.write(eatingArray, 0, numbytes);

				if (numbytes < CHUNKSIZE)
				{ // If the buffer wasn't full, wait a short amount of time to let it fill up
					try
					{
						Thread.sleep(STREAM_SLEEP_TIME);
					}
					catch (InterruptedException e)
					{
						// ignore
					}
				}

				// Run the monitor every MONITOR_UPDATE_INTERVAL runs through the loop
				if (0 == ++loops % MONITOR_UPDATE_INTERVAL)
				{
					monitor.progress(totalSize);
				}
			}
		}
		catch (IOException e)
		{
			log.error("[StreamUtil] {streamCopy} IO Exception: " + e.getMessage(), e);

			monitor.failure();
			throw new IOError(e);
		}
		catch (Error e)
		{
			monitor.failure();
			throw e;
		}
		finally
		{
			monitor.complete();
		}
	}


	public static void streamCopy(final Reader reader, final Writer writer) throws IOException
	{
		Reader in = (reader.getClass() == BufferedReader.class) ? reader : new BufferedReader(reader);
		try
		{
			Writer out = (writer.getClass() == BufferedWriter.class) ? writer : new BufferedWriter(writer);
			try
			{

				final char[] buffer = new char[4096];

				int read = 0;
				while ((read = in.read(buffer)) != -1)
				{
					out.write(buffer, 0, read);
				}
			}
			finally
			{
				out.close();
			}
		}
		finally
		{
			in.close();
		}
	}


	/**
	 * Copies the contents of an InputStream, using the default encoding, into a Writer
	 *
	 * @param input
	 * @param output
	 *
	 * @throws IOException
	 */
	public static void streamCopy(InputStream input, Writer output) throws IOException
	{
		if (input == null)
			throw new IllegalArgumentException("Must provide something to read from");
		if (output == null)
			throw new IllegalArgumentException("Must provide something to write to");

		streamCopy(new InputStreamReader(input), output);
	}
}
