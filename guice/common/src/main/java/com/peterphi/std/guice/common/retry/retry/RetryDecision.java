package com.peterphi.std.guice.common.retry.retry;

public enum RetryDecision
{
	BACKOFF_AND_RETRY,
	LOG_AND_THROW,
	NO_LOG_AND_THROW;
}
