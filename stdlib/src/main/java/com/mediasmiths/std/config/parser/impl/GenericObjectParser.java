package com.mediasmiths.std.config.parser.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.apache.log4j.Logger;

import com.mediasmiths.std.config.ConfigurationFailureError;
import com.mediasmiths.std.config.IConfigLifecycle;
import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.IncompleteConfigurationDefinitionError;
import com.mediasmiths.std.config.annotation.Creatable;
import com.mediasmiths.std.config.annotation.Ignore;
import com.mediasmiths.std.config.annotation.Optional;
import com.mediasmiths.std.config.annotation.StaticCreatable;
import com.mediasmiths.std.config.parser.IConfigParser;
import com.mediasmiths.std.config.parser.ParserFactory;
import com.mediasmiths.std.config.parser.TypeAndClass;

/**
 * Parser for generic objects which uses Java Reflection to read data from value providers
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GenericObjectParser<T> implements IConfigParser<T> {
	private static transient final Logger log = Logger.getLogger(GenericObjectParser.class);

	public static final IConfigParser INSTANCE = new GenericObjectParser();


	/**
	 * @see com.mediasmiths.std.config.parser.IConfigParser#read(java.lang.Class, com.mediasmiths.std.config.IContextValueProvider)
	 */
	@Override
	public T read(ParserFactory factory, TypeAndClass<T> type, boolean required, IContextValueProvider provider) {

		// Swap to the real implementing class if necessary
		{
			final Class<T> c = type.clazz;
			final String implementingClassName = provider.get("class", c.getName());

			if (implementingClassName.compareTo(c.getName()) != 0) {
				try {
					type = new TypeAndClass(Class.forName(implementingClassName));
				}
				catch (ClassNotFoundException e) {
					log.error("{read} Can't read " + implementingClassName + " at " + provider.getContextForErrorLog("class") + ": " +
							e.getMessage(), e);
				}
			}
		}

		// Now try to read the object
		try {
			T ret = readObject(factory, type, 0, required, provider);

			if (ret == null && required) {
				throw new IncompleteConfigurationDefinitionError(provider);
			}

			return ret;
		}
		catch (IncompleteConfigurationDefinitionError e) {
			if (required)
				throw e;
			else
				return null;
		}
		catch (ConfigurationFailureError e) {
			throw e;
		}
		catch (Throwable t) {
			throw new ConfigurationFailureError(t.getMessage(), t);
		}
	}


	private Class readClassfield(Class current, int depth, IContextValueProvider provider) {
		final String currentName = (current != null) ? current.getName() : null;

		final String desired = provider.get(getClassfieldForDepth(depth), currentName);

		if (currentName == desired)
			return current;
		else if (currentName == null || !desired.equals(currentName)) {
			try {
				return Class.forName(desired, true, getClass().getClassLoader());
			}
			catch (Throwable t) {
				log.error(
						"[ObjectParser] {readObjectReal} Error executing class.forName on " + desired +
						" which is the real class specified for field " + provider.getContextForErrorLog() + ". Error: " +
						t.getMessage(),
						t);

				throw new ConfigurationFailureError("Error executing class.forName on " + desired +
						" which is the real class specified for field " + provider.getContextForErrorLog() + ". Error: " +
						t.getMessage(), t);
			}
		}
		else {
			return current;
		}
	}


	private T readObject(
			final ParserFactory parserFactory,
			final TypeAndClass<T> type,
			final int creatableDepth,
			final boolean required,
			final IContextValueProvider values) throws Throwable {
		if (type.clazz.equals(Object.class))
			throw new IllegalArgumentException("Cannot read bare Objects! At " + values.getContextForErrorLog());

		// Resolve the class which implements this object (given its creatable depth)
		final Class c = readClassfield(type.clazz, creatableDepth, values);

		Class creatable = getCreatable(c);

		if (creatable == null) {
			creatable = readClassfield(null, creatableDepth + 1, values);
		}
		if (creatable != null) {
			Object object = readObject(parserFactory, new TypeAndClass(creatable), creatableDepth + 1, required, values);

			if (object == null) {
				if (!required)
					return null;
				else
					throw new IncompleteConfigurationDefinitionError(values);
			}
			else {
				// Get the appropriate Creatable constructor
				Constructor<T> constructor = getConstructor(c, object.getClass());

				try {
					T instance = constructor.newInstance(object);

					// Handle config lifecycle
					constructed(instance);

					return instance;
				}
				catch (Throwable e) {
					log.error("[ObjectParser] {readObject} Error calling Creatable constructor : " + e.getMessage(), e);
					throw new ConfigurationFailureError("Error calling Creatable constructor for " + c, e);
				}
			}
		}
		else {
			if (creatableDepth == 0) {
				return readObjectReal(parserFactory, type, required, values);
			}
			else {
				IConfigParser<T> parser = parserFactory.getProvider(c);

				if (!parser.getClass().equals(this.getClass())) {
					return parser.read(parserFactory, type, required, values);
				}
				else {
					return readObjectReal(parserFactory, type, required, values);
				}
			}

		}
	}


	/**
	 * Find a constructor which will take an argument of a particular type
	 * 
	 * @param c
	 * @param argType
	 * @return
	 */
	private Constructor<T> getConstructor(Class<T> c, Class argType) {
		for (Constructor<?> constructor : c.getConstructors()) {
			final Class[] args = constructor.getParameterTypes();

			if (args != null && args.length == 1 && args[0].isAssignableFrom(argType)) {
				return (Constructor<T>) constructor;
			}
		}

		// No constructor found
		throw new ConfigurationFailureError("Couldn't find constructor for " + c + " which takes " + argType);
	}


	private static final String getClassfieldForDepth(int creatableDepth) {
		String classfield = "class";

		if (creatableDepth != 0) {
			for (int i = 0; i < creatableDepth; i++) {
				classfield = "creatable." + classfield;
			}
		}

		return classfield;
	}


	private T readObjectReal(
			final ParserFactory factory,
			final TypeAndClass<T> type,
			final boolean required,
			final IContextValueProvider provider) throws Throwable {
		final Class<T> c = type.clazz;

		Field[] fields = c.getFields();

		if (c.isInterface()) {
			if (required)
				throw new ConfigurationFailureError("Non-disambiguated interface; cannot parse field " +
						provider.getContextForErrorLog());
			else {
				if (log.isTraceEnabled())
					log.trace("[ObjectParser] {readObjectReal} Encountered non-concreted optional field " +
							provider.getContextForErrorLog() + ". Assuming null");

				return null;
			}
		}

		T instance;
		try {
			instance = c.newInstance();
		}
		catch (InstantiationException e) {
			if (!required) {
				return null;
			}
			else {
				throw new ConfigurationFailureError("No default constructor available for " + c.getName() +
						" when reading from config at: " + provider.getContextForErrorLog());
			}
		}

		for (Field field : fields) {
			if (shouldProcessField(field)) {

				provider.pushContext(field);
				try {
					final Class fieldClass = field.getType();
					final TypeAndClass fieldType = new TypeAndClass(field);
					final IConfigParser parser = factory.getProvider(fieldClass, field);

					boolean isOptional = isOptional(field);

					boolean fieldRequired = required && !isOptional;

					// Parse the field value
					final Object value = parser.read(factory, fieldType, fieldRequired, provider);

					// If we encounter a null field on a non-optional field this object cannot be constructed
					// If we encounter a null value in an optional primitive field, ignore it: null cannot be assigned to a primitive field
					// If we encounter any non-null value, assign its value and continue to the next field

					if (value == null) {
						if (isOptional) {
							// Field is optional so we can leave it at its default value
						}
						else {
							if (log.isTraceEnabled()) {
								log.trace("[ObjectParser] {readObjectReal} Required field missing, so this object cannot be read. Field: " +
										provider.getContextForErrorLog());
							}

							return null;
						}
					}
					else {
						field.set(instance, value);
					}
				}
				finally {
					provider.popContext(field);
				}
			}
		}

		// Handle config lifecycle
		constructed(instance);

		return instance;
	}


	private void constructed(Object instance) {
		// TODO should configured objects be able to replace themselves? This may lead to overly complicated situations...
		if (instance != null) {
			if (instance instanceof IConfigLifecycle) {
				IConfigLifecycle lifecycleObject = (IConfigLifecycle) instance;

				lifecycleObject.initialized();
			}
		}
	}


	private boolean isOptional(Field f) {
		return f.isAnnotationPresent(Optional.class);
	}


	private boolean shouldProcessField(Field f) {
		int modifiers = f.getModifiers();

		// Only process instance fields which are non-static and which are not annotated with the @Ignore annotation
		return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !f.isAnnotationPresent(Ignore.class);
	}


	// @SuppressWarnings("deprecation")
	private Class getCreatable(Class<T> c) {

		Creatable creatableAnnotation = c.getAnnotation(Creatable.class);

		if (creatableAnnotation != null && creatableAnnotation.value() != null) {
			return creatableAnnotation.value();
		}
		else if (StaticCreatable.has(c)) {
			return StaticCreatable.get(c);
		}

		// No known Creatable for this type
		return null;
	}


	@Override
	public boolean canParse(Class c) {
		return true; // will parse anything
	}

}
