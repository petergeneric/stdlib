package com.peterphi.std.util;

import com.peterphi.std.io.PropertyFile;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

/**
 * Retrieve the MANIFEST.MF properties for the jar/war containing a particular class
 */
public class ClassManifestLocator
{
	private static final Logger log = Logger.getLogger(ClassManifestLocator.class);


	/**
	 * Attempt to find the MANIFEST.MF associated with a particular class
	 *
	 * @param clazz
	 * 		The class whose jar/war should be searched for a MANIFEST.MF
	 *
	 * @return a PropertyFile version of the main manifest attributes if found, otherwise null
	 */
	public static PropertyFile get(Class<?> clazz)
	{
		try
		{
			// If we get a guice-enhanced class then we should go up one level to get the class name from the user's code
			if (clazz.getName().contains("$$EnhancerByGuice$$"))
				clazz = clazz.getSuperclass();

			final String classFileName = clazz.getSimpleName() + ".class";
			final String classFilePathAndName = clazz.getName().replace('.', '/') + ".class";

			URL url = clazz.getResource(classFileName);

			if (log.isTraceEnabled())
				log.trace("getResource(" + classFileName + ") = " + url);

			if (url == null)
			{
				return null;
			}
			else
			{
				String classesUrl = url.toString();

				// Get the classes base
				classesUrl = classesUrl.replace(classFilePathAndName, "");

				// Special-case: classes in a webapp are at /WEB-INF/classes/ rather than /
				if (classesUrl.endsWith("WEB-INF/classes/"))
				{
					classesUrl = classesUrl.replace("WEB-INF/classes/", "");
				}

				final URL manifestURL = new URL(classesUrl + "META-INF/MANIFEST.MF");

				try
				{
					final InputStream is = manifestURL.openStream();

					try
					{
						final PropertyFile props = new PropertyFile();
						final Manifest manifest = new Manifest(is);

						for (Object key : manifest.getMainAttributes().keySet())
						{
							final Object value = manifest.getMainAttributes().get(key);

							props.set(key.toString(), value.toString());
						}

						return props;
					}
					finally
					{
						IOUtils.closeQuietly(is);
					}
				}
				catch (FileNotFoundException e)
				{
					log.warn("Could not find: " + manifestURL, e);

					return null;
				}
			}
		}
		catch (Throwable t)
		{
			log.warn("Error acquiring MANIFEST.MF for " + clazz, t);

			return null;
		}
	}
}
