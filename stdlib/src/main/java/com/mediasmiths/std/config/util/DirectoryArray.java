package com.mediasmiths.std.config.util;

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import com.mediasmiths.std.config.Configuration;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.annotation.Creatable;

/**
 * 
 * @param <T> the type of object stored in this array (to simplify retrieval)
 */
@Creatable(DirectoryArrayConfig.class)
@SuppressWarnings({"unchecked"})
public class DirectoryArray<T> {
	private final DirectoryArrayConfig config;
	private final File directoryContext;

	private List<T> content = null;

	private Map<File, Long> timestamps = null;


	public DirectoryArray(DirectoryArrayConfig config) {
		this.config = config;
		this.directoryContext = ThreadConfigurationFileContext.peek();

		reload();
	}


	/**
	 * Reloads the configuration objects
	 */
	public void reload() {
		final File[] files = getFiles();

		load(files);
	}


	/**
	 * Returns true if it appears that the configuration files this object loaded have changed<br />
	 * The <code>reload</code> method can reload the configuration from disk
	 * 
	 * @return
	 */
	public boolean hasChanged() {
		final File[] files = getFiles();

		if (timestamps == null)
			return true; // we don't have anything!
		else if (timestamps.size() != files.length)
			return true; // number of files on disk have changed
		else if (!this.timestamps.equals(getLastModified(files)))
			return true; // Either file names or their timestamps have changed

		return false;
	}


	protected Map<File, Long> getLastModified(File[] files) {
		final Map<File, Long> timestamps = new HashMap<File, Long>();

		for (File file : files)
			timestamps.put(file, file.lastModified());

		return timestamps;
	}


	protected void load(File[] files) {
		final List<T> content = new ArrayList<T>();
		final Map<File, Long> timestamps = getLastModified(files);

		// Sort the files (this will sort them ascending by their filename) to give some predictability in ordering when the user wants a List/Array
		Arrays.sort(files);

		for (File file : files) {
			try {
				T item = (T) Configuration.get(config.type, file);

				content.add(item);
			}
			catch (IncompleteConfigurationDefinitionError e) {
				throw new IllegalStateException("Failed to read " + config.type + " from " + file + ": " + e.getMessage(), e);
			}
		}

		// Verify that the size constraints are valid
		if (content.size() > config.max)
			throw new IllegalStateException("DirectoryArray found " + content.size() + " in " + config.directory +
					" which is greater than the max permitted in the configuration");
		else if (content.size() < config.min)
			throw new IllegalStateException("DirectoryArray found " + content.size() + " in " + config.directory +
					" which is lower than the min permitted in the configuration");

		// Everything checks out
		this.content = content;
		this.timestamps = timestamps;
	}


	protected File getDirectory() {
		if (config.directory.isAbsolute())
			return config.directory;
		else {
			final File context = this.directoryContext;

			if (context == null)
				throw new IllegalStateException(
						"Relative directory reference used in DirectoryArray but the configuration stack does not have a parent File listed (was the Configuration provider told about the File or just passed a Value Provider?)");
			else
				return new File(context.getParentFile(), config.directory.getPath());
		}
	}


	protected File[] getFiles() {
		final File directory = getDirectory();

		if (!directory.exists())
			throw new IllegalStateException("DirectoryArray cannot work with directory that does not exist: " + directory);

		final File[] files = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				final File f = new File(dir, name);

				return f.isFile() && (!name.startsWith(".") && !name.endsWith(".tmp") && !name.endsWith(".temp")) &&
						config.isPermitted(dir, name);
			}
		});

		if (files != null)
			return files;
		else
			return new File[0];
	}


	/**
	 * Get an ordered List of the configuration objects<br />
	 * This List is ordered alphabetically, ascending, by the complete filepath of the configuration file it came from
	 * 
	 * @return an ordered List of the configuration objects
	 */
	public List<T> getList() {
		return new ArrayList<T>(content);
	}


	/**
	 * Get an ordered Array of the configuration objects<br />
	 * This Array is ordered alphabetically, ascending, by the complete filepath of the configuration file it came from<br />
	 * This depends on the value of the generic parameter T being the same as the configured parameter <code>type</code>. This method may fail with a ClassCastException if the Configuration's defined type and T are not compatible<br />
	 * For safety users are recommended to use the getArray method which takes a type instead.
	 * 
	 * @return an ordered Array of the configuration objects
	 */
	public T[] getArray() {
		return (T[]) getArray(config.type);
	}


	/**
	 * Get an ordered Array of the configuration objects<br />
	 * This Array is ordered alphabetically, ascending, by the complete filepath of the configuration file it came from
	 * 
	 * @param type the type for the array
	 * @return an ordered Array of the configuration objects
	 */
	public <A extends T> A[] getArray(Class<A> type) {
		// Get the contents
		final List<T> list = getList();

		// Construct an array & copy the List to it
		A[] aArray = (A[]) Array.newInstance(type, list.size());
		aArray = list.toArray(aArray);

		return aArray;
	}


	public Set<T> getSet() {
		return new HashSet<T>(content);
	}
}
