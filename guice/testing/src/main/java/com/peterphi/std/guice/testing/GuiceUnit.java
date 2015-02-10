package com.peterphi.std.guice.testing;

import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.internal.runners.util.FrameworkUsageValidator;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A JUnit runner for the guice framework; test classes are generally annotated with {@link
 * com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig} to indicate the desired guice
 * environment customisation.
 * <p/>
 * This implementation is based on Jukito's JukitoRunner
 */
public class GuiceUnit extends BlockJUnit4ClassRunner
{
	private final GuiceRegistry registry;


	/**
	 * Creates a BlockJUnit4ClassRunner to run {@code clazz}
	 *
	 * @param clazz
	 *
	 * @throws org.junit.runners.model.InitializationError
	 * 		if the test class is malformed.
	 */
	public GuiceUnit(final Class<?> clazz) throws InitializationError
	{
		super(clazz);

		registry = GuiceRegistryBuilder.createRegistry(getTestClass());
	}


	@Override
	protected Object createTest() throws Exception
	{
		return registry.getInjector().getInstance(getTestClass().getJavaClass());
	}


	@Override
	protected Statement methodBlock(FrameworkMethod method)
	{
		final Statement testLogic = super.methodBlock(method);

		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				testLogic.evaluate();

				// Now shut down the guice environment
				registry.stop();
			}
		};
	}


	@Override
	public void run(final RunNotifier notifier)
	{
		// add listener that validates Mockito mocks at the end of each test
		notifier.addListener(new FrameworkUsageValidator(notifier));
		super.run(notifier);
	}


	protected Statement methodInvoker(FrameworkMethod method, Object test)
	{
		return new GuiceAwareInvokeStatement(registry, method, test);
	}


	/**
	 * Wrap a statement with logic that must run before it executes
	 *
	 * @param method
	 * @param target
	 * @param statement
	 *
	 * @return
	 *
	 * @deprecated deprecated in {@link org.junit.runners.BlockJUnit4ClassRunner} and going away in a future version. Should be
	 * changed to use Rules.
	 */
	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement)
	{
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Before.class);

		if (methods.isEmpty())
			return statement;
		else
			return new GuiceAwareBeforeAfterStatement(statement, methods, null, target, registry);
	}


	/**
	 * Wrap a statement with logic that must run after it executes
	 *
	 * @param method
	 * @param target
	 * @param statement
	 *
	 * @return
	 *
	 * @deprecated deprecated in {@link org.junit.runners.BlockJUnit4ClassRunner} and going away in a future version. Should be
	 * changed to use Rules.
	 */
	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement)
	{
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(After.class);
		if (methods.isEmpty())
			return statement;
		else
			return new GuiceAwareBeforeAfterStatement(statement, null, methods, target, registry);
	}


	@Override
	protected String testName(FrameworkMethod method)
	{
		final Doc annotation = method.getMethod().getAnnotation(Doc.class);

		if (annotation != null)
		{
			return StringUtils.join(annotation.value(), "\n");
		}
		else
		{
			return super.testName(method);
		}
	}


	@Override
	protected List<FrameworkMethod> computeTestMethods()
	{
		return TestEachUtils.computeTestMethods(getTestClass(), registry);
	}


	/**
	 * Adds to {@code errors} for each method annotated with {@code @Test},
	 * {@code @Before}, or {@code @After} that is not a public, void instance
	 * method with no arguments.
	 *
	 * @deprecated deprecated in {@link org.junit.runners.BlockJUnit4ClassRunner} and going away in a future version
	 */
	@Override
	@Deprecated
	protected void validateInstanceMethods(List<Throwable> errors)
	{
		validatePublicVoidMethods(After.class, false, errors);
		validatePublicVoidMethods(Before.class, false, errors);
		validateTestMethods(errors);
	}


	/**
	 * Adds to {@code errors} for each method annotated with {@code @Test}that
	 * is not a public, void instance method.
	 * <p/>
	 * <b>Note:</b> it is permitted to have arguments (superclass requires that methods
	 * have no arguments)
	 */
	@Override
	protected void validateTestMethods(List<Throwable> errors)
	{
		validatePublicVoidMethods(Test.class, false, errors);
	}


	/**
	 * Adds to {@code errors} if any method in this class is annotated with
	 * the provided {@code annotation}, but:
	 * <ul>
	 * <li>is not public, or</li>
	 * <li>returns something other than void, or</li>
	 * <li>is static (given {@code isStatic is false}), or</li>
	 * <li>is not static (given {@code isStatic is true}).</li>
	 * </ul>
	 *
	 * @see #validatePublicVoidNoArgMethods(Class, boolean, java.util.List) validatePublicVoidNoArgMethods for a no-arg version of
	 * this validation
	 */
	protected void validatePublicVoidMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors)
	{
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

		for (FrameworkMethod eachTestMethod : methods)
		{
			eachTestMethod.validatePublicVoid(isStatic, errors);
		}
	}
}
