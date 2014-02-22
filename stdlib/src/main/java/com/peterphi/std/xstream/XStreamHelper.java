package com.peterphi.std.xstream;

import com.peterphi.std.types.BooleanMessage;
import com.peterphi.std.xstream.serialisers.InetAddressConverter;
import com.peterphi.std.xstream.serialisers.URIConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * Title: XStream Helper Class
 * </p>
 * <p/>
 * <p>
 * Description: Provides some static helper functions for dealing with serialisation
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2006-2009
 * </p>
 * <p/>
 * <p>
 * <p/>
 * </p>
 *
 * @version $Revision$
 */
@SuppressWarnings({"rawtypes"})
public class XStreamHelper
{
	private static XSHelper _xs;

	private static boolean cacheXStream = true;
	private static boolean registerStandardTypeConverters = true;
	private static boolean noObjectGraph = true;

	private XStreamHelper()
	{
	} // Prevent instantiation


	/**
	 * Enables or disabled object graph serialisation (whether the same object is duplicated or linked in the output XML
	 * document)
	 *
	 * @param val
	 * 		True to disable object graph serialisation, otherwise false to enable it
	 */
	public synchronized static void setNoObjectGraph(boolean val)
	{
		// If we're caching XStream instances & this value changes, flush the cache
		if (cacheXStream && val != noObjectGraph)
		{
			_xs = null;
		}

		noObjectGraph = val;
	}


	/**
	 * Returns the current state of object graph serialisation
	 *
	 * @return True if object graph serialisation has been disabled, otherwise true if it is enabled
	 */
	public synchronized static boolean getNoObjectGraph()
	{
		return noObjectGraph;
	}


	/**
	 * Sets whether the standard type converters (eg. InetAddressConverter, etc.) should be registered. When changed, any cached
	 * xstream instance will be discarded
	 *
	 * @param val
	 * 		boolean - if true, the typ
	 */
	public synchronized static void setRegisterStandardTypeConverters(boolean val)
	{

		if (registerStandardTypeConverters != val)
		{
			if (cacheXStream)
			{
				_xs = null;
			}
		}

		registerStandardTypeConverters = val;
	}


	/**
	 * Determines whether standard type converters have been enabled
	 *
	 * @return
	 */
	public synchronized static boolean getRegisterStandardTypeConverters()
	{
		return registerStandardTypeConverters;
	}


	/**
	 * Enables or disables the caching of XStream instances internally When changed any cached xstream instance will be discarded
	 *
	 * @param val
	 */
	public synchronized static void setCacheXstream(boolean val)
	{
		if (val != cacheXStream)
		{
			_xs = null;
		}
		cacheXStream = val;
	}


	/**
	 * Determines whether XStream instance caching is currently enabled
	 *
	 * @return
	 */
	public synchronized static boolean getCacheXstream()
	{
		return cacheXStream;
	}


	/**
	 * Sets the internal XStream instance to use (provided caching is enabled)
	 *
	 * @param xs
	 *
	 * @throws Error
	 * 		if XStream caching is not enabled
	 */
	public synchronized static void setXs(XSHelper xs)
	{
		if (cacheXStream)
		{
			_xs = xs;
		}
		else
		{
			throw new Error("Can only set the default xstream object when caching is enabled");
		}
	}


	/**
	 * Returns an XStream instance. This will cache & register converters as necessary
	 *
	 * @return
	 */
	private synchronized static XSHelper getXs()
	{
		return getXs(cacheXStream);
	}


	/**
	 * Returns an XStream instance. This will cache & register converters as necessary
	 *
	 * @return
	 */
	private synchronized static XSHelper getXs(boolean cached)
	{
		XSHelper xs = cached ? _xs : null;

		if (xs == null)
		{
			xs = XSHelper.create();

			if (registerStandardTypeConverters)
			{
				xs.registerConverter(new URIConverter());
				xs.registerConverter(new InetAddressConverter());
			}

			xs.setNoObjectGraph(noObjectGraph);

			// If we are to cache the xstream object:
			if (cached)
			{
				_xs = xs;
			}
		}

		return xs;
	}


	public synchronized static XSHelper create()
	{
		return getXs(false);
	}


	public synchronized static void useAttributeFor(Class<? extends Object> definedIn, String fieldName)
	{
		if (cacheXStream)
		{
			getXs().useAttributeFor(definedIn, fieldName);
		}
		else
		{
			throw new Error("Can only set attributes for fields when xstream caching is enabled");
		}
	}


	/**
	 * Adds an alias to the internally cached XStream instance
	 *
	 * @param alias
	 * 		The alias name
	 * @param aliasedClass
	 * 		The class to be aliased
	 *
	 * @throws Error
	 * 		when XStream caching is disabled
	 */
	public synchronized static void addAlias(String alias, Class aliasedClass)
	{
		if (cacheXStream)
		{
			getXs().alias(alias, aliasedClass);
		}
		else
		{
			throw new Error("Can only add aliases when xstream caching is enabled");
		}
	}


	/**
	 * Adds an alias (including a default implementation) to the internally cached XStream instance
	 *
	 * @param alias
	 * 		The alias name
	 * @param aliasedClass
	 * 		The class to be aliased
	 * @param defaultImplementation
	 * 		The default implementation
	 *
	 * @throws Error
	 * 		when XStream caching is disabled
	 */
	public synchronized static void addAlias(String alias, Class aliasedClass, Class defaultImplementation)
	{
		if (cacheXStream)
		{
			getXs().alias(alias, aliasedClass, defaultImplementation);
		}
		else
		{
			throw new Error("Can only add aliases when xstream caching is enabled");
		}
	}


	/**
	 * Registers a Converter with the internal XStream instance
	 *
	 * @param converter
	 * 		The converter to register
	 *
	 * @throws Error
	 * 		when XStream caching is disabled
	 */
	public synchronized static void registerConverter(Converter converter)
	{
		if (cacheXStream)
		{
			getXs().registerConverter(converter);
		}
		else
		{
			throw new Error("Can only add converters when xstream caching is enabled");
		}
	}


	/**
	 * Uses the default XStream instance to serialise an object to a String
	 *
	 * @param o
	 * 		The object to serialise
	 *
	 * @return The serialised object or null if an exception occurred. All exceptions are logged
	 */
	public static String serialise(Object o)
	{
		return getXs().serialise(o);
	}


	/**
	 * Uses the default XStream instance to serialise an object to a File
	 *
	 * @param f
	 * 		The file to serialise to
	 * @param o
	 * 		The object to serialise
	 *
	 * @return True if the serialisation succeeded, otherwise false and an error message
	 */
	public static BooleanMessage serialise(File f, Object o)
	{
		return getXs().serialise(f, o);
	}


	/**
	 * Uses the default XStream instance to serialise an object to an OutputStream
	 *
	 * @param os
	 * 		the stream to output to
	 * @param o
	 * 		The object to serialise
	 *
	 * @return True if the serialisation succeeded, otherwise false and an error message
	 */
	public static BooleanMessage serialise(OutputStream os, Object o)
	{
		return getXs().serialise(os, o);
	}


	/**
	 * Deserialises an Object from a File
	 *
	 * @param f
	 * 		The file to deserialise
	 *
	 * @return The object (or null if the deserialisation failed)
	 */
	public static Object deserialise(File f)
	{
		return getXs().deserialise(f);
	}


	/**
	 * Uses the default XStream instance to deserialise an XML document into an Object
	 *
	 * @param xml
	 * 		The XML document to deserialise
	 *
	 * @return The object (or null if an exception occurred). All exceptions are logged.
	 */
	public static Object deserialise(String xml)
	{
		return getXs().deserialise(xml);
	}


	/**
	 * Deserialises an InputStream into an Object using the default XStream instance
	 *
	 * @param stream
	 * 		The InputStream
	 *
	 * @return The Object (or null if an exception occurred). All exceptions are logged.
	 */
	public static Object deserialise(InputStream stream)
	{
		return getXs().deserialise(stream);
	}


	/**
	 * Helper method to allow easy testing of the type of a serialised type. It's not recommended that you use this method
	 *
	 * @param xs
	 * 		the XStream instance to use
	 * @param s
	 * 		The string representing the serialised object
	 *
	 * @return
	 */
	public static Class typeOf(XStream xs, String s)
	{
		Object o = xs.fromXML(s);

		if (o == null)
			return null;
		else
			return o.getClass();
	}


	/**
	 * Performs a deep clone. See <code>XSHelper.clone</code>
	 *
	 * @param <T>
	 * @param obj
	 *
	 * @return
	 */
	public static <T> T clone(T obj)
	{
		return getXs().clone(obj);
	}
}
