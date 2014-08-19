package com.peterphi.std.guice.testing;

import com.google.common.collect.Iterables;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.stringparsing.StringToTypeConverter;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestEach;
import org.apache.log4j.Logger;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Method Invoker statement that can fill in method parameters using Guice
 */
class GuiceAwareInvokeStatement extends Statement
{
	private static final Logger log = Logger.getLogger(GuiceAwareInvokeStatement.class);

	private final GuiceRegistry registry;
	private final FrameworkMethod method;
	private final Object test;


	public GuiceAwareInvokeStatement(final GuiceRegistry registry, final FrameworkMethod method, final Object test)
	{
		this.registry = registry;
		this.method = method;
		this.test = test;
	}


	@Override
	public void evaluate() throws Throwable
	{
		List<Object> params = new ArrayList<>();
		if (method.getMethod().getParameterTypes().length > 0)
		{
			final Method m = method.getMethod();
			final Errors errors = new Errors(m);

			for (int paramIndex = 0; paramIndex < method.getMethod().getParameterTypes().length; paramIndex++)
			{
				final Annotation[] annotations = m.getParameterAnnotations()[paramIndex];
				final TestEach annotation = TestEachUtils.getAnnotation(annotations, TestEach.class);

				if (annotation != null)
				{
					// Value from a collection param
					final Object param = getCollectionParamValue(errors, paramIndex, annotation);

					params.add(param);
				}
				else
				{
					// Regular parameter
					final TypeLiteral<?> type = TypeLiteral.get(m.getGenericParameterTypes()[paramIndex]);
					final Key<?> key = Annotations.getKey(type, m, annotations, errors);

					params.add(registry.getInjector().getInstance(key));
				}
			}

			errors.throwCreationExceptionIfErrorsExist();
		}

		// Call the method
		method.invokeExplosively(test, params.toArray(new Object[params.size()]));
	}


	/**
	 * Compute the value for a parameter annotated with
	 *
	 * @param errors
	 * @param paramIndex
	 *
	 * @return
	 *
	 * @throws ErrorsException
	 */
	private Object getCollectionParamValue(final Errors errors,
	                                       final int paramIndex,
	                                       final TestEach annotation) throws ErrorsException
	{
		if (!(method instanceof TestEachFrameworkMethod))
			throw new AssertionError("Required a parameterised FrameworkMethod but got " + method);

		// The index within the collection to use for this particular invocation
		final int collectionIndex = ((TestEachFrameworkMethod) method).getCollectionIndexForParameter(paramIndex);

		if (annotation.value() != null && annotation.value().length > 0)
		{
			final Class<?> desiredType = method.getMethod().getParameterTypes()[paramIndex];
			final String val = annotation.value()[collectionIndex];

			return convertParamType(val, desiredType);
		}
		else
		{
			return getGuiceCollectionParamValue(errors, paramIndex, collectionIndex);
		}
	}


	private Object convertParamType(final String val, final Class<?> desiredType)
	{
		return StringToTypeConverter.convert(desiredType, val);
	}


	private Object getGuiceCollectionParamValue(final Errors errors,
	                                            final int paramIndex,
	                                            final int collectionIndex) throws ErrorsException
	{
		Collection<?> col = TestEachUtils.getGuiceCollectionForParam(method.getMethod(), paramIndex, registry, errors);

		// We have found the binding; use the iterator to fetch the desired index
		return Iterables.get(col, collectionIndex);
	}
}

