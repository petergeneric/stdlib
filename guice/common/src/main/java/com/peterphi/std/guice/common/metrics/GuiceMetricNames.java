package com.peterphi.std.guice.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.annotation.Doc;

public final class GuiceMetricNames
{
	// @Retry annotations
	@Doc("The number of @Retry annotated methods that have been called")
	public static final String RETRY_CALLS = "feature.auto-retry.calls";
	@Doc("The number of tries (including the first try) made on @Retry annotated methods")
	public static final String RETRY_ATTEMPTS = "feature.auto-retry.attempts";
	@Doc("The number of tries (including the first try) made on @Retry annotated methods that have failed")
	public static final String RETRY_ATTEMPT_FAILURES = "feature.auto-retry.attempt-failures";
	@Doc("The number of @Retry annotated methods that have failed despite retries")
	public static final String RETRY_TOTAL_FAILURES = "feature.auto-retry.failures";

	// JAX-RS calls
	public static final String HTTP_CALLS_TIMER = "feature.jax-rs-http-server.calls";
	public static final String HTTP_EXCEPTIONS_METER = "feature.jax-rs-http-server.calls.non-404-exception";
	public static final String HTTP_404_EXCEPTIONS_METER = "feature.jax-rs-http-server.calls.404-exception";
	public static final String HTTP_IGNORED_CLIENT_ABORTS = "feature.jax-rs-http-server.calls.ignored-client-abort";

	// @Transaction annotations
	@Doc("The number of calls to a @Transactional method (including calls that do not start a transaction)")
	public static final String TRANSACTION_CALLS_TIMER = "feature.transactional.all-calls";
	@Doc("The number of calls to a @Transactional method that start a transaction")
	public static final String TRANSACTION_OWNER_CALLS_TIMER = "feature.transaction.owner-calls";
	@Doc("The number of calls to a @Transactional method that result in a transaction rollback")
	public static final String TRANSACTION_ERROR_ROLLBACK_METER = "feature.transaction.rollback.exception";
	@Doc("The number of calls to a @Transactional method where the commit has been attempted but failed")
	public static final String TRANSACTION_COMMIT_FAILURE_METER = "feature.transaction.commit.failures";

	// Thymeleaf templating
	public static final String THYMELEAF_CALL_TIMER = "feature.thymeleaf.calls";
	public static final String THYMELEAF_RENDER_EXCEPTION_METER = "feature.thymeleaf.exception";

	// @AuthConstraint interception
	@Doc("Keeps track of the number of requests that have been made")
	public static final String AUTH_CONSTRAINT_CALL_METER = "feature.AuthConstraint.calls";
	@Doc("Keeps track of the number of requests that have been granted")
	public static final String AUTH_CONSTRAINT_GRANTED_METER = "feature.AuthConstraint.allow";
	@Doc("Keeps track of the number of requests that have been denied")
	public static final String AUTH_CONSTRAINT_DENIED_METER = "feature.AuthConstraint.deny";
	@Doc("Keeps track of the number of requests from logged in users (i.e. non-anonymous) which have been denied")
	public static final String AUTH_CONSTRAINT_AUTHENTICATED_DENIED_METER = "feature.AuthConstraint.authenticated-deny";

	//@Cache annotations
	@Doc("The number of @Cache annotated methods that have returned a cached result")
	public static final String CACHE_HITS = "cache.hits";
	@Doc("The number of @Cache annotated methods that have had to calculate a result")
	public static final String CACHE_MISSES = "cache.misses";


	private GuiceMetricNames()
	{
	}


	/**
	 * Produces a sensible name for a path beneath a class, taking into account that the class provided might be enhanced by
	 * guice
	 * AOP (if it is then the class from the code will be used, rather than the generated AOP class name)
	 *
	 * @param clazz
	 * @param names
	 *
	 * @return
	 */
	public static String name(Class<?> clazz, String... names)
	{
		// If we get a guice-enhanced class then we should go up one level to get the class name from the user's code
		if (clazz.getName().contains("$$EnhancerByGuice$$"))
			clazz = clazz.getSuperclass();

		return MetricRegistry.name(clazz, names);
	}
}
