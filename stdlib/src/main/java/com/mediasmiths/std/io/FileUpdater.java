package com.mediasmiths.std.io;

import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * A helper class which simplifies the tasks involved in rewriting a configuration file, writing to a temp file and then safely replacing the old file with the new
 */
public class FileUpdater {
	private static transient final Logger log = Logger.getLogger(FileUpdater.class);

	private final File file;
	private final File tempFile;

	private OutputStream os;
	private InputStream is;
	boolean committedOrRolledBack = false;


	public FileUpdater(File file) {
		this.file = file;
		this.tempFile = createTempFilename("update");
	}


	public BufferedReader openReader() throws IOException {
		return new BufferedReader(new InputStreamReader(openInputStream()));
	}


	public InputStream openInputStream() throws IOException {
		if (is == null)
			is = new FileInputStream(file);

		return is;
	}


	public BufferedWriter openWriter() throws IOException {
		return new BufferedWriter(new OutputStreamWriter(openOutputStream()));
	}


	/**
	 * Get a snapshot of the temporary file as it currently exists; this may be the original file or it may be a copy of it.<br />
	 * The only reliable way to determine
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getTemporaryFile() throws IOException {
		return tempFile;
	}


	public OutputStream openOutputStream() throws IOException {
		if (os == null)
			os = new FileOutputStream(tempFile);

		return os;
	}


	public void close() {
		try {
			if (os != null)
				os.close();
		}
		catch (IOException e) {
			log.error("[FileUpdater] {close} Error closing output file: " + e.getMessage(), e);
		}
		finally {
			os = null;
		}

		try {
			if (is != null)
				is.close();
		}
		catch (IOException e) {
			log.error("[FileUpdater] {close} Error closing input file: " + e.getMessage(), e);
		}
		finally {
			is = null;
		}
	}


	public void commit() throws IOException {
		if (committedOrRolledBack)
			throw new Error("Cannot commit an already committed/rolled back update");

		close();

		final File moveaside = createTempFilename("moveaside");

		committedOrRolledBack = true;

		if (file.renameTo(moveaside)) {
			if (tempFile.renameTo(file)) {
				if (!moveaside.delete()) {
					log.warn("[FileUpdater] {commit} Error deleting original (moveaside) file: " + moveaside.getAbsolutePath());
				}
			}
			else {
				if (moveaside.renameTo(file)) {
					tempFile.delete();
					throw new IOException("Error moving temp->current; rolled back to original");
				}
				else
					throw new IOException("Error moving temp->current; error moving original->current");

			}
		}
		else {
			tempFile.delete();
			throw new IOException("Error moving current->original (moveaside); state has not changed");

		}
	}


	public void rollback() {
		if (committedOrRolledBack)
			throw new Error("Cannot rollback an already committed/rolled back update");
		close();

		tempFile.delete();

		committedOrRolledBack = true;
	}


	private File createTempFilename(String use) {
		use = FileHelper.enforceSafeFilename(use);
		String fileName = FileHelper.enforceSafeFilename(file.getName());

		return new File(file.getParentFile(), "." + use + "_" + fileName + "_" + UUID.randomUUID() + ".tmp");
	}


	@Override
	public void finalize() {
		if (!committedOrRolledBack) {
			log.warn("[FileUpdater] {finalize} Object ref let go without commit/rollback call. Rolling back.");
			rollback();
		}
	}
}
