package com.peterphi.std.guice.common.cached.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.common.cached.CacheManager;
import com.peterphi.std.threading.Timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around a single item guava cache as a workaround for google not implementing <a
 * href="https://github.com/google/guava/issues/872">the request for a single item cache made in 2012</a>.
 * <p>
 * This is a light wrapper around {@link Cache}
 *
 * @param <T> the type of value cached
 */
public class SingleItemCache<T>
{
	/**
	 * The static key used for the cache "map"
	 */
	private static final Integer KEY = 0;

	private final Cache<Integer, T> cache;
	private final Callable<T> supplier;
	private String name;


	/**
	 * Create a new cache, caching the result of <code>supplier</code> for a maximum of <code>validity</code>. A new Cache will be
	 * constructed using {@link CacheBuilder}
	 *
	 * @param supplier a function that generates some cache-worthy result; see {@link #get()} for information on how/when this
	 *                 method is called. The supplier MUST NOT return null
	 * @param validity the time after write that the cached result from <code>supplier</code> should expire
	 */
	public SingleItemCache(Callable<T> supplier, Timeout validity)
	{
		this(CacheBuilder.newBuilder().expireAfterWrite(validity.getMilliseconds(), TimeUnit.MILLISECONDS), supplier);
	}


	/**
	 * Create a new cache, caching the result of <code>supplier</code> using a cache built by <code>builder</code>.
	 *
	 * @param builder  the cache builder; will be largely untouched, although the maximum size will be set to 1 (since the cache
	 *                 will only store a single item)
	 * @param supplier a function that generates some cache-worthy result; see {@link #get()} for information on how/when this
	 *                 method is called. The supplier MUST NOT return null
	 */
	public SingleItemCache(CacheBuilder builder, Callable<T> supplier)
	{
		this(builder, supplier, true);
	}


	/**
	 * Create a new cache, caching the result of <code>supplier</code> using a cache built by <code>builder</code>.
	 *
	 * @param builder         the cache builder; will be largely untouched, although the maximum size will be set to 1 (since the
	 *                        cache will only store a single item)
	 * @param supplier        a function that generates some cache-worthy result; see {@link #get()} for information on how/when
	 *                        this method is called. The supplier MUST NOT return null
	 * @param useCacheManager if true, will not be registered with the CacheManager to allow cache to be cleared manually
	 */
	public SingleItemCache(CacheBuilder builder, Callable<T> supplier, final boolean useCacheManager)
	{
		this.supplier = supplier;
		this.cache = builder.maximumSize(1).build();

		if (useCacheManager)
		{
			// Assign a name and register it with the Cache Manager
			setName("unnamed single item cache");
		}
	}


	/**
	 * Get the value if it's sitting in the cache, otherwise return null
	 *
	 * @return the value from the cache, or null if no value was cached
	 * @see Cache#getIfPresent(Object)
	 */
	public T getIfPresent()
	{
		return cache.getIfPresent(KEY);
	}


	/**
	 * Return the cached value (or regenerate it).
	 *
	 * @return the cached result, or a newly-generated value if the cache had expired (or had been actively invalidated)
	 * @throws RuntimeException if the supplier function passed to the constructor throws
	 * @see Cache#get(Object, Callable)
	 */
	public T get()
	{
		try
		{
			return cache.get(KEY, supplier);
		}
		catch (ExecutionException e)
		{
			final Throwable cause = e.getCause();

			// Try to rethrow the actual exception so it's easier to understand
			if (cause == null)
				throw new RuntimeException(e);
			else if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;
			else if (cause instanceof Error)
				throw (Error) cause;
			else
				throw new RuntimeException("Unexpected error loading item into cache: " + cause.getMessage(), cause);
		}
	}


	/**
	 * Invalidate any cached result, such that subsequent calls to {@link #get()} will re-generate the value
	 *
	 * @see Cache#invalidateAll()
	 */
	public void invalidate()
	{
		cache.invalidateAll();
	}


	public void setName(final String name)
	{
		this.name = name;

		CacheManager.register(name, cache);
	}


	public String getName()
	{
		return name;
	}


	@Override
	public String toString()
	{
		return "SingleItemCache{name=" + name + ",supplier=" + supplier + "}";
	}
}
