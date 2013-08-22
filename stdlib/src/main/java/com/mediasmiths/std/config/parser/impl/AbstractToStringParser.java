package com.mediasmiths.std.config.parser.impl;

import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;

@SuppressWarnings("rawtypes")
public abstract class AbstractToStringParser implements IConfigParser<Object> {

	/**
	 * @see com.mediasmiths.std.config.parser.IConfigParser#read(java.lang.Class, com.mediasmiths.std.config.IContextValueProvider)
	 */
	@Override
	public Object read(ParserFactory factory, TypeAndClass<Object> c, boolean required, IContextValueProvider provider) {
		String defaultValue = null;

		final String value = provider.get(defaultValue);

		// Assume the parsed value is null if no value is provided
		final Object data;
		if (value != null)
			data = parse(c.clazz, value);
		else
			data = null;

		if (data == null && required)
			throw new IncompleteConfigurationDefinitionError(provider);
		else
			return data;
	}


	protected abstract Object parse(Class t, String val);


	@Override
	public abstract boolean canParse(Class c);
}
