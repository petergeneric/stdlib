package com.peterphi.std.guice.common;

import com.google.common.base.Predicate;
import org.apache.log4j.Logger;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.apache.xbean.finder.filter.PrefixFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class ClassScanner
{
	private static final Logger log = Logger.getLogger(ClassScanner.class);

	private final AnnotationFinder finder;

	private final long constructionTime;
	private final AtomicLong searchTime = new AtomicLong(0);


	private ClassScanner(AnnotationFinder finder, long constructionTime)
	{
		this.finder = finder;
		this.constructionTime = constructionTime;
	}


	public long getConstructionTime()
	{
		return constructionTime;
	}


	public long getSearchTime()
	{
		return searchTime.get();
	}


	/**
	 * Find all the classes in a package
	 *
	 * @param pkg
	 * @param recursive
	 * @param predicate
	 * 		an optional additional predicate to filter the list against
	 *
	 * @return
	 */
	public List<Class<?>> getClasses(final String pkg, boolean recursive, final Predicate<Class<?>> predicate)
	{
		final long started = System.currentTimeMillis();

		try
		{
			return filter(finder.findClassesInPackage(pkg, recursive), predicate);
		}
		finally
		{
			final long finished = System.currentTimeMillis();
			searchTime.addAndGet(finished - started);

			if (log.isTraceEnabled())
				log.trace("getClasses " +
				          pkg +
				          " with predicate=" +
				          predicate +
				          " returned in " +
				          (finished - started) +
				          " ms");
		}
	}


	/**
	 * Find all the classes in a package
	 *
	 * @param pkg
	 * @param recursive
	 *
	 * @return
	 */
	public List<Class<?>> getClasses(final String pkg, boolean recursive)
	{
		return getClasses(pkg, recursive, null);
	}


	/**
	 * Find all the classes that are siblings of the provided class
	 *
	 * @param clazz
	 * 		the class in whose package to search
	 * @param recursive
	 * 		if true, search all the child packages of the package containing the class
	 * @param predicate
	 * 		an optional additional predicate to filter the list against
	 *
	 * @return
	 */
	public List<Class<?>> getSiblingClasses(final Class<?> clazz, boolean recursive, final Predicate<Class<?>> predicate)
	{
		return getClasses(getPackages(clazz)[0], recursive, predicate);
	}


	public List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotation, Predicate<Class<?>> predicate)
	{
		final long started = System.currentTimeMillis();
		try
		{
			return filter(finder.findAnnotatedClasses(annotation), predicate);
		}
		finally
		{
			final long finished = System.currentTimeMillis();
			searchTime.addAndGet(finished - started);

			if (log.isTraceEnabled())
				log.trace("getAnnotatedClasses " +
				          annotation +
				          " with predicate=" +
				          predicate +
				          " returned in " +
				          (finished - started) +
				          " ms");
		}
	}


	public List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotation)
	{
		return getAnnotatedClasses(annotation, null);
	}


	public List<Class<?>> getInheritedAnnotatedClasses(Class<? extends Annotation> annotation, Predicate<Class<?>> predicate)
	{
		final long started = System.currentTimeMillis();
		try
		{
			return filter(finder.findInheritedAnnotatedClasses(annotation), predicate);
		}
		finally
		{
			final long finished = System.currentTimeMillis();
			searchTime.addAndGet(finished - started);

			if (log.isTraceEnabled())
				log.trace("getInheritedAnnotatedClasses " +
				          annotation +
				          " with predicate=" +
				          predicate +
				          " returned in " +
				          (finished - started) +
				          " ms");
		}
	}


	public List<Class<?>> getInheritedAnnotatedClasses(Class<? extends Annotation> annotation)
	{
		return getInheritedAnnotatedClasses(annotation, null);
	}


	public <T> List<Class<? extends T>> getExtendingClasses(final Class<T> clazz)
	{
		return getExtendingClasses(clazz, null);
	}


	public <T> List<Class<? extends T>> getExtendingClasses(final Class<T> clazz, Predicate<Class<? extends T>> predicate)
	{
		final long started = System.currentTimeMillis();
		try
		{
			return filter(finder.findImplementations(clazz), predicate);
		}
		finally
		{
			final long finished = System.currentTimeMillis();
			searchTime.addAndGet(finished - started);

			if (log.isTraceEnabled())
				log.trace("getExtendingClasses " +
				          clazz +
				          " with predicate=" +
				          predicate +
				          " returned in " +
				          (finished - started) +
				          " ms");
		}
	}


	private <T> List<T> filter(List<T> list, Predicate<T> predicate)
	{
		// If predicate has been provided then apply it against everything in the list
		if (predicate != null)
		{
			final Iterator<T> it = list.iterator();

			while (it.hasNext())
			{
				if (!predicate.apply(it.next()))
					it.remove();
			}
		}

		return list;
	}


	public static ClassScanner forPackages(String... packages)
	{
		return forPackages(Thread.currentThread().getContextClassLoader(), packages);
	}


	public static ClassScanner forPackages(Class<?>... classes)
	{
		return forPackages(getPackages(classes));
	}


	public static ClassScanner forPackages(ClassLoader classloader, String... packages)
	{
		final long started = System.currentTimeMillis();


		CompositeArchive archive = getArchivesForPackage(classloader, packages);

		AnnotationFinder finder = new AnnotationFinder(archive, true);

		final long finished = System.currentTimeMillis();

		return new ClassScanner(finder, finished - started);
	}


	private static String[] getPackages(Class<?>... classes)
	{
		Set<String> set = new HashSet<>(classes.length);

		for (Class<?> clazz : classes)
			set.add(clazz.getPackage().getName());

		return set.toArray(new String[set.size()]);
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

					log.info("Found source: " + url);

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


	public static Predicate<Class<?>> annotatedWith(final Class<? extends Annotation> annotation)
	{
		return new Predicate<Class<?>>()
		{
			@Override
			public boolean apply(final Class<?> input)
			{
				return input.isAnnotationPresent(annotation);
			}
		};
	}


	public static Predicate<Class<?>> packagePredicate(final Predicate<String> packagePredicate)
	{
		return new Predicate<Class<?>>()
		{
			@Override
			public boolean apply(final Class<?> input)
			{
				return packagePredicate.apply(input.getPackage().getName());
			}
		};
	}


	public static Predicate<Class<?>> interfaceClass()
	{
		return new Predicate<Class<?>>()
		{
			@Override
			public boolean apply(final Class<?> input)
			{
				return input.isInterface();
			}
		};
	}
}
