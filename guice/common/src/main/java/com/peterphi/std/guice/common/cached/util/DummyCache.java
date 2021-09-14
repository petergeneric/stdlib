package com.peterphi.std.guice.common.cached.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * A dummy cache which can be used to hook into a cache clear operation
 */
public abstract class DummyCache implements Cache<Object, Object>
{
	@Nullable
	@Override
	public Object getIfPresent(final Object key)
	{
		return null;
	}


	@Override
	public Object get(final Object key, final Callable loader) throws ExecutionException
	{
		// ignore
		return null;
	}


	@Override
	public void put(final Object key, final Object value)
	{
		// ignore
	}


	@Override
	public void invalidate(final Object key)
	{
		this.invalidateAll();
	}


	@Override
	public abstract void invalidateAll();


	@Override
	public long size()
	{
		return 0;
	}


	@Override
	public CacheStats stats()
	{
		return new CacheStats(0, 0, 0, 0, 0, 0);
	}


	@Override
	public ConcurrentMap asMap()
	{
		return new ConcurrentHashMap(0);
	}


	@Override
	public void cleanUp()
	{

	}


	@Override
	public void invalidateAll(final Iterable keys)
	{
		this.invalidateAll();
	}


	@Override
	public void putAll(final Map m)
	{

	}


	@Override
	public ImmutableMap getAllPresent(final Iterable keys)
	{
		return ImmutableMap.of();
	}
}
