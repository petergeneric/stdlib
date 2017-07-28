package com.peterphi.std.guice.common;

import org.apache.log4j.Logger;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.apache.xbean.finder.filter.PrefixFilter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * A Factory for {@link com.peterphi.std.guice.common.ClassScanner} instances that maintains a {@link
 * java.lang.ref.WeakReference}
 * to the produced scanner (allowing the GC to collect the produced scanner and associated jar file refs when it's no longer in
 * use)
 */
public class ClassScannerFactory
{
	private static final Logger log = Logger.getLogger(ClassScannerFactory.class);

	private WeakReference<ClassScanner> cached = null;

	private ClassLoader classloader;
	private String[] packages;

	private int constructions = 0;
	private long constructionTime = 0;


	public ClassScannerFactory(String... packages)
	{
		this(null, packages);
	}


	public ClassScannerFactory(ClassLoader loader, String... packages)
	{
		this.classloader = loader;
		this.packages = packages;
	}


	public ClassScanner getInstance()
	{
		if (packages.length == 0)
			return null; // there are no packages to scan

		ClassScanner scanner = cached != null ? cached.get() : null;

		// Lazy create scanner if necessary
		if (scanner == null)
		{
			ClassLoader loader = classloader;

			if (loader == null)
				loader = Thread.currentThread().getContextClassLoader();

			scanner = forPackages(loader, packages);

			this.cached = new WeakReference<>(scanner);
			constructions++;
		}

		return scanner;
	}


	private static ClassScanner forPackages(ClassLoader classloader, String... packages)
	{
		final long started = System.currentTimeMillis();

		CompositeArchive archive = getArchivesForPackage(classloader, packages);

		AnnotationFinder finder = new AnnotationFinder(archive, true);

		final long finished = System.currentTimeMillis();

		return new ClassScanner(finder, finished - started);
	}


	private static CompositeArchive getArchivesForPackage(final ClassLoader classloader, final String... packages)
	{
		try
		{
			final List<Archive> archives = new ArrayList<>();

			for (String pkg : packages)
			{
				if (!pkg.endsWith("."))
					pkg += "."; // Add a trailing dot for easier package matching

				final String baseFolder = pkg.replace('.', '/');

				final Enumeration<URL> urls = classloader.getResources(baseFolder);

				while (urls.hasMoreElements())
				{
					final URL url = urls.nextElement();

					if (log.isTraceEnabled())
						log.trace("Found source: " + url);

					if (url.getProtocol() != null && (url.getProtocol().equals("zip") || url.getProtocol().equals("jar")))
						archives.add(new FilteredArchive(new JarArchive(classloader, url), new PrefixFilter(pkg)));
					else
						archives.add(new FileArchive(classloader, url, pkg));
				}
			}

			return new CompositeArchive(archives);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Error loading archives for packages: " + Arrays.asList(packages), e);
		}
	}


	public int getMetricNewInstanceCount()
	{
		return this.constructions;
	}
}
