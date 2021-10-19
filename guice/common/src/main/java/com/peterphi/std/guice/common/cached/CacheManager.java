package com.peterphi.std.guice.common.cached;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.common.cached.util.SingleItemCache;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Holds weak references to a number of caches, allowing a UI to list all cached data or to invalidate all cached data
 */
public final class CacheManager
{
	/**
	 * N.B. WeakHashMap weakly references to keys, strong references to values
	 */
	private static WeakHashMap<Cache, String> CACHES = new WeakHashMap<>();


	/**
	 * Build and register a new cache; the cache will be referenced weakly, and the name provided referenced strongly
	 *
	 * @param name    non-unique descriptive name of the cache
	 * @param builder a builder upon which .build() should be invoked
	 * @param <K>     cache key type
	 * @param <V>     cache value type
	 * @return
	 */
	public static <K, V, K1 extends K, V1 extends V> Cache<K1, V1> build(final String name, final CacheBuilder<K, V> builder)
	{
		final Cache<K1, V1> cache = builder.build();

		return register(name, cache);
	}


	/**
	 * Register a new cache; the cache will be referenced weakly, and the name provided referenced strongly
	 *
	 * @param name  non-unique descriptive name of the cache
	 * @param cache the cache object to register
	 * @param <K>   cache key type
	 * @param <V>   cache value type
	 * @return
	 */
	public static <K, V, T extends Cache<K,V>> T register(final String name, final T cache)
	{
		synchronized (CACHES)
		{
			CACHES.put(cache, name);
		}

		return cache;
	}


	/**
	 * Register a SingleItemCache; this is strictly unnecessary because a
	 *
	 * @param name
	 * @param cache
	 * @param <T>
	 * @return
	 */
	public static <T> SingleItemCache<T> register(final String name, final SingleItemCache<T> cache)
	{
		// This will trigger a re-register
		cache.setName(name);

		return cache;
	}


	public static void invalidateAll()
	{
		Map<Cache, String> caches = getCaches();

		for (Cache cache : caches.keySet())
		{
			if (cache != null)
				cache.invalidateAll();
		}
	}


	/**
	 * Used to remove all caches from the manager; this should not normally be needed
	 */
	public static void forgetAllCaches()
	{
		synchronized (CACHES)
		{
			CACHES.clear();
		}
	}


	/**
	 * Retrieve caches grouped by their name; there can be a lot of caches with the same name (e.g. <code>SessionScoped</code>
	 * caches)
	 *
	 * @return
	 */
	public static Map<String, List<Cache>> getCachesByName()
	{
		Map<Cache, String> caches = getCaches();

		Map<String, List<Cache>> ret = new HashMap<>();

		for (Map.Entry<Cache, String> entry : caches.entrySet())
		{
			final String name = entry.getValue();
			final Cache cache = entry.getKey();

			List<Cache> list = ret.get(name);

			if (list == null)
			{
				list = new ArrayList<>();
				ret.put(name, list);
			}

			list.add(cache);
		}

		return ret;
	}


	/**
	 * @return
	 * @throws RuntimeException if we got too many ConcurrentModificationException errors trying to take a copy of the map of
	 *                          caches
	 */
	public static Map<Cache, String> getCaches()
	{
		synchronized (CACHES)
		{
			int attempts = 128;
			while (attempts-- > 0)
			{
				try
				{
					return new HashMap<>(CACHES);
				}
				catch (ConcurrentModificationException e)
				{
					// The GC must have removed some entries, try again immediately
				}
			}
		}

		throw new RuntimeException(
				"Garbage Collection activity prevented us from being able to reliably read from the list of caches, please try again later");
	}


	private CacheManager()
	{
	}
}
