package com.mediasmiths.std.config.parser.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;
import com.mediasmiths.std.config.values.NoArrayException;

@SuppressWarnings({"rawtypes","unchecked"})
public class MapParser implements IConfigParser<Map> {
	/**
	 * @see com.mediasmiths.std.config.parser.IConfigParser#read(java.lang.Class, com.mediasmiths.std.config.IContextValueProvider)
	 */
	@Override
	public Map read(ParserFactory factory, TypeAndClass<Map> type, boolean required, IContextValueProvider values) {
		// final Class componentClass = getComponent(type);
		final Class keyClass = readClass(values, "keyClass", getComponent(type, 0));
		final Class valueClass = readClass(values, "valueClass", getComponent(type, 1));

		return readMap(factory, keyClass, valueClass, required, values);
	}


	private Class readClass(IContextValueProvider values, String fieldName, Class defaultValue) {
		final String className = values.get(fieldName, null);

		if (className == null)
			return defaultValue;

		try {
			return Class.forName(className);
		}
		catch (Throwable t) {
			throw new IllegalArgumentException("Cannot load class name: " + className);
		}
	}


	private <K, V> Map<K, V> readMap(
			ParserFactory factory,
			Class<K> keyClass,
			Class<V> valueClass,
			boolean required,
			IContextValueProvider values) {
		final int minSize = Integer.parseInt(values.get("min", "0"));
		final int maxSize = Integer.parseInt(values.get("max", Integer.toString(Integer.MAX_VALUE)));

		Map<K, V> map = new HashMap<K, V>();

		// For value providers which support list autobounding, require the element
		final boolean subscriptRequired = values.supportsListAutoBounding();

		for (int subscript = 0; subscript < Integer.MAX_VALUE; subscript++) {
			try {
				values.setContextSubscript(subscript);
			}
			catch (NoArrayException e) {
				if (required)
					throw new IncompleteConfigurationDefinitionError(values, e);
				else
					return null;
			}
			catch (IndexOutOfBoundsException e) {
				if (values.supportsListAutoBounding()) {
					// We have come to the end of the array
					// This is expected
					break;
				}
				else {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			factory.getProvider(keyClass);

			final K key = readField(factory, subscriptRequired, values, keyClass, "key");
			final V val = readField(factory, subscriptRequired, values, valueClass, "value");

			if (key != null) {
				map.put(key, val);
			}
		}

		// Check the size constraints
		if (map.size() < minSize)
			throw new IllegalStateException("Too few items in Array " + values.getContextForErrorLog() + ": min " + minSize);
		if (map.size() > maxSize)
			throw new IllegalStateException("Too many items in Array " + values.getContextForErrorLog() + ": max " + maxSize);

		return map;
	}


	private <T> T readField(
			final ParserFactory factory,
			final boolean required,
			final IContextValueProvider values,
			final Class<T> clazz,
			final String fieldName) {
		final IConfigParser<T> parser = factory.getProvider(clazz);

		values.pushContext(fieldName);
		try {
			return parser.read(factory, new TypeAndClass<T>(clazz), required, values);
		}
		finally {
			values.popContext(fieldName);
		}
	}


	@Override
	public boolean canParse(Class c) {
		return c.equals(Map.class) || c.equals(HashMap.class);
	}


	public static Class getComponent(TypeAndClass t, int index) {
		if (t.type instanceof ParameterizedType) {
			ParameterizedType p = (ParameterizedType) t.type;

			Type[] args = p.getActualTypeArguments();

			if (args.length <= index)
				throw new IllegalArgumentException("Cannot read generic argument " + index + " from " + t.type + ": type only has " +
						args.length);
			else if (args[index] instanceof Class)
				return (Class) args[index];
			else
				throw new IllegalArgumentException("Generic argument " + index + " from " + t.type +
						": could not be treated as a Class!");
		}

		return Object.class; // unknown generic parameter
	}
}