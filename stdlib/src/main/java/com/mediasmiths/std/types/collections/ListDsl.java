package com.mediasmiths.std.types.collections;

import java.util.*;
import java.lang.reflect.Array;
import com.mediasmiths.std.types.*;

/**
 * Based on Joe Wright's collection DSL at <a href="http://code.joejag.com/2011/a-dsl-for-collections-in-java/">http://code.joejag.com/2011/a-dsl-for-collections-in-java/</a>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ListDsl {
	/**
	 * Converts the provided Collection to an Array of the specified type
	 * 
	 * @param <T>
	 * @param clazz
	 * @param collection
	 * @return
	 */
	public static <T> T[] array(final Class<T> clazz, final Collection<? extends T> collection) {
		final int size = size(collection);

		// Reflectively create an array of the correct type
		final T[] array = (T[]) Array.newInstance(clazz, size);

		return array(array, collection);
	}


	public static <T> T[] array(final T[] array, final Collection<? extends T> collection) {
		if (size(collection) != 0)
			return collection.toArray(array);
		else
			return array;
	}


	public static <T> int size(T[] array) {
		if (array != null)
			return array.length;
		else
			return 0;
	}


	public static <T> int size(Collection<T> coll) {
		if (coll != null)
			return coll.size();
		else
			return 0;
	}


	/**
	 * Strips all nulls from an Iterable
	 * 
	 * @param <T>
	 * @param iterable
	 */
	public static <T, C extends Iterable<T>> C stripnull(final C iterable) {
		if (iterable == null)
			throw new IllegalArgumentException("Must provide a non-null Iterable to strip nulls from!");

		final Iterator<T> it = iterable.iterator();

		while (it.hasNext()) {
			final T item = it.next();

			if (item == null)
				it.remove();
		}

		return iterable;
	}


	/**
	 * Strips all nulls from an Array, returning an Array
	 * 
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> T[] stripnull(final T[] array) {
		if (array == null)
			throw new IllegalArgumentException("Must provide a non-null array to strip nulls from!");

		// search for the first null value
		for (T item : array) {
			if (item == null) {
				final Class<T> clazz = (Class<T>) array.getClass().getComponentType();

				return array(clazz, stripnull(list(array)));
			}
		}

		// no nulls found
		return array;
	}


	/**
	 * Returns the first item in a collection (or null if the collection is empty or null)
	 * 
	 * @param <T>
	 * @param itbl the object to iterate over
	 * @return the first item in the collection (or null if the collection is empty or null)
	 */
	public static <T> T first(Iterable<T> itbl) {
		if (itbl != null)
			for (T item : itbl)
				return item;

		return null;
	}


	/**
	 * Returns the first item in an array (or null if the array is empty or null)
	 * 
	 * @param <T>
	 * @param array
	 * @return the first item in the array (or null if the array is empty or null)
	 */
	public static <T> T first(T[] array) {
		if (array != null && array.length != 0)
			return array[0];
		else
			return null;
	}


	/**
	 * Returns a copy of the items contained in the provided list. if the list is null then an empty list is returned
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> List<T> list(List<T> list) {
		if (list != null)
			return list;
		else
			return new ArrayList<T>();
	}


	/**
	 * Returns a copy of the items contained in the provided list. if the list is null then an empty list is returned
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> List<T> list(Collection<T> coll) {
		if (coll != null)
			return new ArrayList<T>(coll);
		else
			return new ArrayList<T>();
	}


	public static List list() {
		return new ArrayList(0);
	}


	/**
	 * Returns a copy of the items contained in the provided array. if no items are provided then an empty list is returned
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> List<T> list(T... args) {
		if (size(args) > 0) {
			List<T> list = new ArrayList<T>(args.length);

			Collections.addAll(list, args);
			return list;
		}
		else {
			return new ArrayList<T>(0);
		}
	}


	public static <T> Set<T> set(T... args) {
		if (size(args) > 0) {
			Set<T> set = new HashSet<T>(args.length);

			Collections.addAll(set, args);
			return set;
		}
		else {
			return new HashSet<T>();
		}
	}


	public static <T> Set<T> set(Set<T> set) {
		if (set != null)
			return new HashSet<T>(set);
		else
			return new HashSet<T>();
	}


	public static <T> Set<T> set(Collection<T> coll) {
		if (coll != null)
			return new HashSet<T>(coll);
		else
			return new HashSet<T>();
	}


	public static <K, V> Map<K, V> map(Both<? extends K, ? extends V>... entries) {
		final Map<K, V> result = new HashMap<K, V>(entries.length);

		for (Both<? extends K, ? extends V> entry : entries)
			if (entry != null)
				result.put(entry.getLeft(), entry.getRight());

		return result;
	}


	public static <K, V> Map<K, V> set(Map<K, V> map) {
		if (map != null)
			return new HashMap<K, V>(map);
		else
			return new HashMap<K, V>();
	}
}