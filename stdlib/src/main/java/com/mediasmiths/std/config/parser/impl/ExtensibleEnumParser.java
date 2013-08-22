package com.mediasmiths.std.config.parser.impl;

import java.lang.reflect.Method;
import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.annotation.ExtensibleEnum;
import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;

@SuppressWarnings("rawtypes")
public class ExtensibleEnumParser implements IConfigParser<Object> {
	/**
	 * @see com.mediasmiths.std.config.parser.IConfigParser#read(java.lang.Class, com.mediasmiths.std.config.IContextValueProvider)
	 */
	@Override
	public Object read(
			final ParserFactory factory,
			final TypeAndClass<Object> type,
			final boolean required,
			final IContextValueProvider values) {
		final Class c = type.clazz;

		final String value = values.get(null);

		if (value == null) {
			if (required)
				throw new IncompleteConfigurationDefinitionError(values);
			else
				return null;
		}
		else {
			try {
				@SuppressWarnings("unchecked")
				final Method method = c.getMethod("valueOf", String.class);

				return method.invoke(null, value);
			}
			catch (Exception e) {
				throw new RuntimeException("Error trying to construct instance of " + c.getName() +
						" using static valueOf(String) method:" + e.getMessage(), e);
			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean canParse(Class c) {
		return c.isAnnotationPresent(ExtensibleEnum.class);
	}
}
