package com.peterphi.std.io;

import com.ice.tar.TarArchive;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/*
 * <p> Title: Archive Helper </p>
 * 
 * <p> Description: Provides assistance in working with archives (and compressed archives) </p>
 * 
 * <p> Copyright: Copyright (c) 2006 </p>
 * 
 * <p>  </p>
 * 
 * 
 * @version $Revision$
 */
public class ArchiveHelper
{
	private static Logger log = Logger.getLogger(ArchiveHelper.class);


	/**
	 * Gets the input stream for a given file (for tar archives)
	 *
	 * @param f
	 * 		File
	 *
	 * @return InputStream
	 *
	 * @throws FileNotFoundException
	 * 		When the file's not found
	 */
	private static InputStream getInputStream(File f) throws FileNotFoundException
	{
		InputStream is = new FileInputStream(f);
		try
		{
			return new GZIPInputStream(is);
		}
		catch (IOException e)
		{
			return new FileInputStream(f);
		}
	}


	private static TarArchive getArchive(File tarFile) throws FileNotFoundException
	{
		InputStream is = getInputStream(tarFile);

		return new TarArchive(is);
	}


	/**
	 * Extracts a .tar or .tar.gz archive to a given folder
	 *
	 * @param tarFile
	 * 		File The archive file
	 * @param extractTo
	 * 		File The folder to extract the contents of this archive to
	 *
	 * @return boolean True if the archive was successfully extracted, otherwise false
	 */
	public static boolean extractArchive(File tarFile, File extractTo)
	{
		try
		{
			TarArchive ta = getArchive(tarFile);
			try
			{
				if (!extractTo.exists())
					if (!extractTo.mkdir())
						throw new RuntimeException("Could not create extract dir: " + extractTo);

				ta.extractContents(extractTo);
			}
			finally
			{
				ta.closeArchive();
			}
			return true;
		}
		catch (FileNotFoundException e)
		{
			log.error("File not found exception: " + e.getMessage(), e);
			return false;
		}
		catch (Exception e)
		{
			log.error("Exception while extracting archive: " + e.getMessage(), e);
			return false;
		}
	}


	/**
	 * Adds a file or files to a jar file, replacing the original one
	 *
	 * @param jarFile
	 * 		File the jar file
	 * @param basePathWithinJar
	 * 		String the base path to put the files within the Jar
	 * @param files
	 * 		File[] The files. The files will be placed in basePathWithinJar
	 *
	 * @throws Exception
	 * @since 2007-06-07 uses createTempFile instead of Java's createTempFile, increased buffer from 1k to 4k
	 */
	public static boolean addFilesToExistingJar(File jarFile,
	                                            String basePathWithinJar,
	                                            Map<String, File> files,
	                                            ActionOnConflict action) throws IOException
	{

		// get a temp file
		File tempFile = FileHelper.createTempFile(jarFile.getName(), null);

		boolean renamed = jarFile.renameTo(tempFile);
		if (!renamed)
		{
			throw new RuntimeException("[ArchiveHelper] {addFilesToExistingJar} " + "Could not rename the file " +
			                           jarFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}

		ZipInputStream jarInput = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream jarOutput = new ZipOutputStream(new FileOutputStream(jarFile));

		try
		{
			switch (action)
			{
				case OVERWRITE:
					overwriteFiles(jarInput, jarOutput, basePathWithinJar, files);
					break;
				case CONFLICT:
					conflictFiles(jarInput, jarOutput, basePathWithinJar, files);
					break;
				default:
					// This should never happen with validation of action taking place in WarDriver class
					throw new IOException("An invalid ActionOnConflict action was received");
			}
		}
		finally
		{
			if (!tempFile.delete())
				log.warn("Could not delete temp file " + tempFile);
		}
		return true;
	}


	private static void overwriteFiles(ZipInputStream jarInput,
	                                   ZipOutputStream jarOutput,
	                                   String basePathWithinJar,
	                                   Map<String, File> files) throws IOException
	{
		byte[] buf = new byte[4096];
		ZipEntry entry = jarInput.getNextEntry();
		while (entry != null)
		{
			String name = entry.getName();
			boolean addFile = true;
			for (String f : files.keySet())
			{
				String filename = basePathWithinJar + f;
				if (filename.compareTo(name) == 0)
				{
					addFile = false;
					break;
				}
			}
			if (addFile)
			{
				// Add jar entry to output stream.
				jarOutput.putNextEntry(new ZipEntry(name));
				// Transfer bytes from the jar file to the output file
				int len;
				while ((len = jarInput.read(buf)) > 0)
				{
					jarOutput.write(buf, 0, len);
				}
				jarOutput.closeEntry();
			}
			entry = jarInput.getNextEntry();
		}
		// Close the streams
		jarInput.close();

		for (String fname : files.keySet())
		{
			InputStream input = new FileInputStream(files.get(fname));
			try
			{
				// Add ZIP entry to output stream.
				jarOutput.putNextEntry(new ZipEntry(basePathWithinJar + fname));

				// Transfer bytes from the file to the jar file
				int length;
				while ((length = input.read(buf)) > 0)
				{
					jarOutput.write(buf, 0, length);
				}
				// Complete the entry
				jarOutput.closeEntry();
			}
			finally
			{
				input.close();
			}
		}
		// Complete the jar file
		jarOutput.close();
	}


	private static void conflictFiles(ZipInputStream jarInput,
	                                  ZipOutputStream jarOutput,
	                                  String basePathWithinJar,
	                                  Map<String, File> files) throws IOException
	{
		ZipEntry entry = jarInput.getNextEntry();
		byte[] buf = new byte[4096];
		while (entry != null)
		{
			String name = entry.getName();

			for (String f : files.keySet())
			{
				String filename = basePathWithinJar + f;
				if (filename.compareTo(name) == 0)
				{
					jarInput.close();
					jarOutput.close();
					throw new IOException("File already exists in jar - Action set to conflict");
				}
				else
				{
					// Add jar entry to output stream.
					jarOutput.putNextEntry(new ZipEntry(name));
					// Transfer bytes from the jar file to the output file

					int len;
					while ((len = jarInput.read(buf)) > 0)
					{
						jarOutput.write(buf, 0, len);
					}
					jarOutput.closeEntry();
				}
			}
			entry = jarInput.getNextEntry();
		}
		for (String fname : files.keySet())
		{
			InputStream input = new FileInputStream(files.get(fname));
			try
			{
				// Add ZIP entry to output stream.
				jarOutput.putNextEntry(new ZipEntry(basePathWithinJar + fname));
				// Transfer bytes from the file to the jar file
				int length;
				while ((length = input.read(buf)) > 0)
				{
					jarOutput.write(buf, 0, length);
				}
				// Complete the entry
				jarOutput.closeEntry();
			}
			finally
			{
				input.close();
			}
		}
		// Complete the jar file
		jarOutput.close();
	}
}
