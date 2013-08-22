package com.mediasmiths.std.config.parser.impl;

import java.util.*;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;
import com.mediasmiths.std.config.values.NoArrayException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ArrayParser implements IConfigParser<Object> {
	/**
	 * @see com.mediasmiths.std.config.parser.IConfigParser#read(java.lang.Class, com.mediasmiths.std.config.IContextValueProvider)
	 */
	@Override
	public Object read(ParserFactory factory, TypeAndClass<Object> type, boolean required, IContextValueProvider values) {
		// final Class componentClass = getComponent(type);
		final Class componentClass = readClass(values, "componentClass", getComponent(type));

		final Class c = type.clazz;
		if (isArray(c))
			return readArray(factory, componentClass, required, values);
		else if (isList(c)) {
			return readList(factory, componentClass, required, values);
		}
		else if (isSet(c))
			return readSet(factory, componentClass, required, values);
		else
			throw new IllegalArgumentException("Unknown Array type: " + c);
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


	private <T> Set<T> readSet(ParserFactory factory, Class<T> memberType, boolean required, IContextValueProvider values) {
		final List<T> items = realReadItems(factory, memberType, required, values);

		if (items != null)
			return new HashSet<T>(items);
		else
			return null;
	}


	private <T> List<T> readList(ParserFactory factory, Class<T> memberType, boolean required, IContextValueProvider values) {
		return realReadItems(factory, memberType, required, values);
	}


	/**
	 * Fully generic
	 * 
	 * @param <T>
	 * @param componentType
	 * @param values
	 * @return
	 */
	private <T> Object readArray(ParserFactory factory, Class<T> componentType, boolean required, IContextValueProvider values) {
		List<T> items = realReadItems(factory, componentType, required, values);

		if (items != null)
			return toArray(componentType, items);
		else
			return null;
	}


	private <T> List<T> realReadItems(
			ParserFactory factory,
			Class<T> componentType,
			boolean required,
			IContextValueProvider values) {
		final int minSize = Integer.parseInt(values.get("min", "0"));
		final int maxSize = Integer.parseInt(values.get("max", Integer.toString(Integer.MAX_VALUE)));

		List<T> items = new ArrayList<T>();

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

			final Class objType = readClass(values, "class", componentType);

			if (!componentType.isAssignableFrom(objType))
				throw new IllegalArgumentException("Array " + values.getContextForErrorLog() + " of " + componentType +
						" cannot contain a " + objType);

			IConfigParser parser = factory.getProvider(objType);
			T data = (T) parser.read(factory, new TypeAndClass(objType), subscriptRequired, values);

			if (data != null) {
				items.add(data);
			}
			else {
				break;
			}
		}

		// Check the size constraints
		if (items.size() < minSize)
			throw new IllegalStateException("Too few items in Array " + values.getContextForErrorLog() + ": min " + minSize);
		if (items.size() > maxSize)
			throw new IllegalStateException("Too many items in Array " + values.getContextForErrorLog() + ": max " + maxSize);

		return items;
	}


	private <T> Object toArray(Class<T> componentType, List<T> items) {
		Object buffer = Array.newInstance(componentType, items.size());

		if (!componentType.isPrimitive()) {
			buffer = items.toArray((T[]) buffer);
		}
		else {
			// array of primitives (e.g. int[]) so we can't use List.toArray(T[]) but must use our own, slow method
			for (int i = 0; i < items.size(); i++)
				if (items.get(i) != null) // We're not allowed to assign null to a primitive so don't try!
					Array.set(buffer, i, items.get(i));
		}

		return buffer;
	}


	@Override
	public boolean canParse(Class c) {
		return isArray(c) || isList(c) || isSet(c);
	}


	protected boolean isArray(Class c) {
		return c.isArray();
	}


	protected boolean isList(Class c) {
		return c.equals(List.class) || c.equals(ArrayList.class);
	}


	protected boolean isSet(Class c) {
		return c.equals(Set.class) || c.equals(HashSet.class);
	}


	public static Class getComponent(TypeAndClass t) {
		if (t.clazz.isArray())
			return t.clazz.getComponentType();
		else {
			if (t.type instanceof ParameterizedType) {
				ParameterizedType p = (ParameterizedType) t.type;

				Type[] args = p.getActualTypeArguments();

				if (args.length == 1 && args[0] instanceof Class) {
					return (Class) args[0];
				}
			}

			return Object.class; // unknown generic parameter
		}
	}
}
