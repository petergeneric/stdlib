package com.mediasmiths.std.config.values;

import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

import com.mediasmiths.std.io.PropertyFile;

public class PropertyFileValueProvider extends GenericPropertyFileValueProvider {
	private static transient final Logger log = Logger.getLogger(PropertyFileValueProvider.class);

	private final PropertyFile props;


	public PropertyFileValueProvider(PropertyFile p) {
		this.props = p;
	}


	public PropertyFileValueProvider(Properties properties) throws IOException {
		this(new PropertyFile(properties));
	}


	public PropertyFileValueProvider(File file) throws IOException {
		this(new PropertyFile(file));
	}


	public PropertyFileValueProvider(InputStream file) throws IOException {
		this(new PropertyFile(file));
	}


	@Override
	protected String getProperty(String name, String defaultValue) {

		String val = props.get(name, defaultValue);

		if (val != null)
			val = val.trim();

		if (log.isTraceEnabled())
			log.trace("[PropertyFileValueProvider] {get} " + name + " = " + val);

		return val;
	}
}
