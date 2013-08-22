package com.mediasmiths.std.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;
import com.mediasmiths.std.config.util.ThreadConfigurationFileContext;
import com.mediasmiths.std.config.values.PropertyFileValueProvider;
import com.mediasmiths.std.config.values.xml.DOMValueProvider;
import com.mediasmiths.std.io.PropertyFile;

import org.apache.log4j.Logger;

/**
 * Handles the acquisition of Configuration in which services run
 */
public class Configuration {
	private static transient final Logger log = Logger.getLogger(Configuration.class);


	private Configuration() {

	}


	// User-facing methods (modify the thread configuration file context stack)

	/**
	 * Acquires a particular configuration type from XML files stored in the path
	 * 
	 * @param <T>
	 * @param type
	 * @param propFiles
	 * @return
	 */
	public static <T> T get(Class<T> type, String... propFiles) {
		final PropertyFile props;
		if (propFiles != null && propFiles.length > 0)
			props = PropertyFile.find(type.getClassLoader(), propFiles);
		else
			props = PropertyFile.find(type.getClassLoader(), "service.properties");

		return Configuration.get(type, props);
	}


	/**
	 * Acquires a particular configuration type from XML files stored in the path
	 * 
	 * @param <T>
	 * @param type
	 * @param files
	 * @return
	 */
	public static <T> T getXML(Class<T> type, String... files) {
		final DOMValueProvider xml;
		if (files != null && files.length > 0)
			xml = findXML(type.getClassLoader(), files);
		else
			xml = findXML(type.getClassLoader(), "service.xml");

		return Configuration.get(type, xml);
	}


	private static DOMValueProvider findXML(ClassLoader loader, String... names) {
		for (String fileName : names) {
			try {
				if (fileName.startsWith("/")) {
					final File file = new File(fileName);

					if (file.exists() && file.canRead())
						return new DOMValueProvider(file);
				}
				else {
					final URL resource = loader.getResource(fileName);

					if (resource != null) {
						return new DOMValueProvider(resource.openStream());
					}
				}
			}
			catch (Exception e) {
				log.warn("{get} Exception parsing resource " + fileName + ": " + e.getMessage(), e);
			}
		}

		return null;
	}


	public static <T> T get(Class<T> type, PropertyFile props) {
		try {
			ThreadConfigurationFileContext.push(props.getFile());

			return Configuration.realGet(type, new PropertyFileValueProvider(props));
		}
		finally {
			ThreadConfigurationFileContext.pop(props.getFile());
		}
	}


	protected static boolean isXMLFile(File file) {
		if (file.exists() && file.canRead()) {
			try {
				final FileReader fr = new FileReader(file);
				try {
					return (fr.read() == '<');
				}
				finally {
					fr.close();
				}
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Error reading file " + file + ": " + e.getMessage(), e);
			}
		}
		else
			throw new IllegalArgumentException("File does not exist or cannot be read: " + file);
	}


	public static <T> T get(Class<T> type, File file) {
		final boolean isXML = isXMLFile(file);

		try {
			ThreadConfigurationFileContext.push(file);

			if (isXML)
				return getXML(type, file);
			else
				return getProperties(type, file);
		}
		finally {
			ThreadConfigurationFileContext.pop(file);
		}
	}


	/**
	 * Retrieve configuration values from an XML file
	 * 
	 * @param <T>
	 * @param type
	 * @param file
	 * @return
	 */
	private static <T> T getXML(Class<T> type, File file) {
		try {
			return realGet(type, new DOMValueProvider(file));
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Error loading XML configuration file " + file + ": " + e.getMessage(), e);
		}
	}


	private static <T> T getProperties(Class<T> type, File file) {
		PropertyFile propertyFile;
		try {
			propertyFile = PropertyFile.readOnly(file);
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Error loading configuration file " + file + ": " + e.getMessage(), e);
		}

		return Configuration.realGet(type, propertyFile);
	}


	public static <T> T get(Class<T> type, IContextValueProvider valueProvider) {
		try {
			ThreadConfigurationFileContext.push(null);

			return Configuration.realGet(type, valueProvider);
		}
		finally {
			ThreadConfigurationFileContext.pop(null);
		}
	}


	// Internal methods (do not modify the thread configuration file context stack)

	private static <T> T realGet(Class<T> type, PropertyFile props) {
		return Configuration.realGet(type, new PropertyFileValueProvider(props));
	}


	private static <T> T realGet(Class<T> type, IContextValueProvider valueProvider) {
		
		try {
			final TypeAndClass<T> typeAndClass = new TypeAndClass<T>(type);
			
			final ParserFactory factory = ParserFactory.getInstance();

			IConfigParser<T> parser = factory.getProvider(type);

			final T config = type.cast(parser.read(factory, typeAndClass, true, valueProvider));

			if (config == null)
				throw new ConfigurationFailureError("Error retrieving configuration: required root element not present");
			else
				return config;
		}
		catch (ConfigurationFailureError e) {
			log.error("[ContextProvider] {getContext} Error retrieving context: " + e.getMessage(), e);
			throw e;
		}
		catch (Throwable e) {
			log.error("[ContextProvider] {getContext} Error retrieving context: " + e.getMessage(), e);
			throw new ConfigurationFailureError(e.getMessage(), e);
		}

	}
}
