package com.peterphi.std.guice.testing;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.util.Types;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestEach;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utils for working with methods annotated with {@link com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestEach}
 */
class TestEachUtils
{
	private static final Logger log = Logger.getLogger(TestEachUtils.class);


	public static int getCollectionSizeForParam(final Method method,
	                                            final int paramIndex,
	                                            final TestEach annotation,
	                                            final GuiceRegistry registry)
	{
		if (annotation.value().length > 0)
		{
			return annotation.value().length;
		}
		else
		{
			final Errors errors = new Errors(method);

			final Collection<?> col = getGuiceCollectionForParam(method, paramIndex, registry, errors);

			errors.throwCreationExceptionIfErrorsExist();

			return col.size();
		}
	}


	/**
	 * Retrieve a guice collection version of a type parameter (for instance, <code>void x(String y)</code> would result in
	 * searching for a <code>List&lt;String&gt;</code> or a <code>Set&lt;String&gt;</code>. If a {@link
	 * com.google.inject.name.Named} annotation exists on the parameter then this is taken into account also
	 *
	 * @param method
	 * 		the method
	 * @param paramIndex
	 * 		the index of the parameter within the method
	 * @param registry
	 * 		the registry to use for acquiring a Guice environment
	 * @param errors
	 * 		aggregates exceptions
	 *
	 * @return
	 *
	 * @throws ErrorsException
	 * 		If a Key cannot be built for the parameter as one of the Collection types
	 */
	public static Collection<?> getGuiceCollectionForParam(final Method method,
	                                                       final int paramIndex,
	                                                       final GuiceRegistry registry,
	                                                       final Errors errors)
	{
		try
		{
			// Get a List or Set version of this param
			final Annotation[] annotations = method.getParameterAnnotations()[paramIndex];
			final Class<?> paramClass = method.getParameterTypes()[paramIndex];
			final Type paramType;

			if (!paramClass.isPrimitive())
				paramType = method.getGenericParameterTypes()[paramIndex];
			else
				paramType = getBoxedType(paramClass);

			Throwable t = null;
			for (TypeLiteral type : new TypeLiteral[]{TypeLiteral.get(Types.listOf(paramType)),
			                                          TypeLiteral.get(Types.setOf(paramType))})
			{
				final Key<?> key = Annotations.getKey(type, method, annotations, errors);

				try
				{
					// Try to fetch the binding
					final Collection<?> obj = (Collection<?>) registry.getInjector().getInstance(key);

					// We have found the binding
					return obj;
				}
				catch (Exception e)
				{
					t = e;
					// Failed but we can try the next collection if there is one
					log.trace("Error fetching " + key + " binding", e);
				}
			}

			throw new AssertionError("Failed to retrieve Set or List versions of param " + paramIndex + " of " + method, t);
		}
		catch (ErrorsException e)
		{
			throw new AssertionError("Error producing guice Key for param " + paramIndex + " of " + method, e);
		}
	}


	/**
	 * Given a primitive type, return the boxed type
	 *
	 * @param clazz
	 *
	 * @return
	 */
	private static Type getBoxedType(final Class<?> clazz)
	{
		if (clazz.equals(byte.class))
			return Byte.class;
		else if (clazz.equals(char.class))
			return Character.class;
		else if (clazz.equals(short.class))
			return Short.class;
		else if (clazz.equals(int.class))
			return Integer.class;
		else if (clazz.equals(long.class))
			return Long.class;
		else if (clazz.equals(boolean.class))
			return Boolean.class;
		else if (clazz.equals(double.class))
			return Double.class;
		else if (clazz.equals(float.class))
			return Float.class;
		else if (clazz.equals(long.class))
			return Long.class;
		else if (clazz.equals(long.class))
			return Long.class;
		else
			throw new IllegalArgumentException("Do not know boxed type equivalent for " + clazz);
	}


	public static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> clazz)
	{
		for (Annotation annotation : annotations)
			if (clazz.isAssignableFrom(annotation.getClass()))
				return clazz.cast(annotation);

		return null;
	}


	public static List<FrameworkMethod> computeTestMethods(TestClass testClass, GuiceRegistry registry)
	{
		List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
		for (FrameworkMethod method : testClass.getAnnotatedMethods(Test.class))
		{
			Method javaMethod = method.getMethod();

			Map<Integer, Integer> paramRepeats = new HashMap<>();

			for (int paramIndex = 0; paramIndex < javaMethod.getParameterTypes().length; paramIndex++)
			{
				final TestEach annotation = getAnnotation(javaMethod.getParameterAnnotations()[paramIndex], TestEach.class);

				if (annotation != null)
				{
					final int collectionSize = TestEachUtils.getCollectionSizeForParam(javaMethod,
					                                                                   paramIndex,
					                                                                   annotation,
					                                                                   registry);

					paramRepeats.put(paramIndex, collectionSize);
				}
			}

			if (paramRepeats.isEmpty())
			{
				// Just a regular method, no special parameterisation going on
				result.add(method);
			}
			else
			{
				// Produce the product of every parameter in the map

				final int[] countersParamIndex;
				final int[] countersMax;
				final int[] counters;

				// Initialise the arrays
				int totalCombinations = 1;
				{
					countersParamIndex = new int[paramRepeats.size()];
					countersMax = new int[paramRepeats.size()];
					counters = new int[paramRepeats.size()];

					int i = 0;
					for (Map.Entry<Integer, Integer> entry : paramRepeats.entrySet())
					{
						countersParamIndex[i] = entry.getKey();
						countersMax[i] = entry.getValue();
						counters[i] = 0;

						totalCombinations *= countersMax[i];

						i++;
					}
				}

				// Iterate through totalCombinations time
				// After, increment counters modulo countermax

				for (int i = 0; i < totalCombinations; i++)
				{
					// Build a map of countersParamIndex -> counters
					Map<Integer, Integer> map = new HashMap<>();
					for (int j = 0; j < counters.length; j++)
						map.put(countersParamIndex[j], counters[j]);

					result.add(new TestEachFrameworkMethod(javaMethod, map));

					// Increment the values of counters (unless we're about to hit the end of the loop)
					if (i + 1 != totalCombinations)
						increment(counters, countersMax);
				}
			}
		}
		return result;
	}


	private static void increment(int[] val, int[] max)
	{
		final int index = val.length - 1;

		increment(val, max, index);
	}


	private static void increment(int[] val, int[] max, final int index)
	{
		if (val[index] == max[index] - 1)
		{
			// Don't allow an overflow
			if (index == 0)
				throw new RuntimeException("Incrementing " +
				                           Arrays.asList(ArrayUtils.toObject(val)) +
				                           " index " +
				                           index +
				                           " with max " +
				                           Arrays.asList(ArrayUtils.toObject(max)) +
				                           " would result in overflow!");

			// Zero the values at and to the right of index
			for (int i = index; i < val.length; i++)
				val[i] = 0;

			// Now increment the next index
			increment(val, max, index - 1);
		}
		else
		{
			val[index]++;
		}
	}
}
