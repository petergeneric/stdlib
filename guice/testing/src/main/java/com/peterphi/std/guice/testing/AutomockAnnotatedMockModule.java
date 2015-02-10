package com.peterphi.std.guice.testing;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import org.junit.runners.model.FrameworkField;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A module that creates mocks for fields annotated with {@link com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.Automock}
 * in a test class
 */
class AutomockAnnotatedMockModule extends AbstractTestModule
{
	private final Class<?> testClass;
	private final List<FrameworkField> fields;


	public AutomockAnnotatedMockModule(final Class<?> testClass, final List<FrameworkField> fields)
	{
		this.testClass = testClass;
		this.fields = fields;
	}


	@Override
	protected void configure()
	{
		final Errors errors = new Errors(testClass);

		for (FrameworkField field : fields)
		{
			try
			{
				final Field f = field.getField();
				final Key key = Annotations.getKey(TypeLiteral.get(f.getGenericType()), f, field.getAnnotations(), errors);

				bindMock(key, f.getType(), "Automock[" + field.getName() + "] " + key);
			}
			catch (ErrorsException e)
			{
				// Add it to the error list and hold them all until the end
				errors.merge(e.getErrors());
			}
		}

		errors.throwConfigurationExceptionIfErrorsExist();
	}
}
