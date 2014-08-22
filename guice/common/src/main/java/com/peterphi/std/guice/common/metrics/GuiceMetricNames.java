package com.peterphi.std.guice.common.metrics;

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


	private GuiceMetricNames()
	{
	}
}
