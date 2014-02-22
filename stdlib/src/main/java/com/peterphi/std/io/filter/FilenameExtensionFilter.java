package com.peterphi.std.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A FileFilter which matches files by their extensions
 */
public class FilenameExtensionFilter implements FileFilter
{
	private final Set<String> extensions;
	private final boolean caseSensitive;


	/**
	 * Creates a FilenameExtensionFilter with a single extension
	 *
	 * @param extension
	 * 		the extension without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 * @param caseSensitive
	 * 		true if matches should be case sensitive, otherwise false to perform case insensitive matching
	 *
	 * @deprecated use <code>boolean caseSensitive,String... extensions</code> constructor instead
	 */
	@Deprecated
	public FilenameExtensionFilter(String extension, boolean caseSensitive)
	{
		this(caseSensitive, extension);
	}


	/**
	 * Creates a FilenameExtensionFilter with a set of extensions
	 *
	 * @param extensions
	 * 		the extensions without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 * @param caseSensitive
	 * 		true if matches should be case sensitive, otherwise false to perform case insensitive matching
	 *
	 * @deprecated use <code>boolean caseSensitive,Iterable<String> extensions</code> constructor instead
	 */
	@Deprecated
	public FilenameExtensionFilter(Iterable<String> extensions, boolean caseSensitive)
	{
		this(caseSensitive, extensions);
	}


	/**
	 * Creates a FilenameExtensionFilter with a set of extensions
	 *
	 * @param caseSensitive
	 * 		true if matches should be case sensitive, otherwise false to perform case insensitive matching
	 * @param extensions
	 * 		the extensions without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 */
	public FilenameExtensionFilter(boolean caseSensitive, String... extensions)
	{
		this(caseSensitive, Arrays.asList(extensions));
	}


	/**
	 * Creates a case insensitive FilenameExtensionFilter with a set of extensions
	 *
	 * @param extensions
	 * 		the extensions without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 */
	public FilenameExtensionFilter(String... extensions)
	{
		this(Arrays.asList(extensions));
	}


	/**
	 * Creates a case insensitive FilenameExtensionFilter with a set of extensions
	 *
	 * @param extensions
	 * 		the extensions without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 */
	public FilenameExtensionFilter(Iterable<String> extensions)
	{
		this(false, extensions);
	}


	/**
	 * Creates a FilenameExtensionFilter with a set of extensions
	 *
	 * @param caseSensitive
	 * 		true if matches should be case sensitive, otherwise false to perform case insensitive matching
	 * @param extensions
	 * 		the extensions without the dot (e.g. "zip" should be used as the extension for zip files, "tar.gz" should be used as the
	 * 		extension for tar.gz files)
	 */
	public FilenameExtensionFilter(boolean caseSensitive, Iterable<String> extensions)
	{
		this.caseSensitive = caseSensitive;

		this.extensions = new HashSet<String>();

		for (String extension : extensions)
		{
			final String ext = "." + (caseSensitive ? extension : extension.toLowerCase());

			this.extensions.add(ext);
		}
	}


	public boolean accept(final File dir, final String name)
	{
		return accept(name);
	}


	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File pathname)
	{
		return accept(pathname.getName());
	}


	public boolean accept(String name)
	{
		// case insensitive matching?
		if (!caseSensitive)
			name = name.toLowerCase();

		for (String extension : this.extensions)
			if (name.endsWith(extension))
				return true;

		return false;
	}
}
