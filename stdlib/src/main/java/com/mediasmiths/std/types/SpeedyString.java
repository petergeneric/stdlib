package com.mediasmiths.std.types;

import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;

/**
 * String manipulation library which attempts to optimise String manipulation operations.<br />
 * In the event the optimised methods fail, the library guarantees to perform similarly to the native String operations
 * 
 * 
 */
public class SpeedyString {
	private static Field val;
	private static Field off;
	private static Field cnt;

	private static boolean speedy;

	static {
		try {
			// Acquire the field refs
			val = String.class.getDeclaredField("value");
			off = String.class.getDeclaredField("offset");
			cnt = String.class.getDeclaredField("count");

			// Now make them accessible
			val.setAccessible(true);
			off.setAccessible(true);
			cnt.setAccessible(true);

			speedy = true;
		}
		catch (Throwable t) {
			System.out.println("[SpeedyString performance reduced in this JVM]");
			speedy = false;
		}
	}


	public static void main(String[] args) {
		char[] x = new char[1024];
		fastAllocate(x);
	}


	/**
	 * <strong>DO NOT USE THIS UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING!</strong><br />
	 * <strong>WARNING: The String returned by this method may be mutable (so the caller must guarantee not to modify the source array)</strong>
	 * 
	 * @param src The source array
	 * @param offset the offset within the array the string starts at
	 * @param count The length of the string
	 * @return
	 */
	public static String fastAllocate(final char[] src, final int offset, final int count) {
		if (speedy) {
			try {

				String s = new String();

				// Now update the string's internal state
				val.set(s, src);
				off.set(s, offset);
				cnt.set(s, count);

				return s;
			}
			catch (Throwable t) {
			}
		}

		// Fallback to the slow way
		return new String(src, offset, count);
	}


	/**
	 * <strong>DO NOT USE THIS UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING!</strong><br />
	 * <strong>WARNING: The char[] returned by this method <em>must be treated as read-only</em></strong>
	 * 
	 * @param s
	 * @return
	 */
	public static char[] fastGetChars(String s) {
		if (speedy) {
			try {
				char[] string = (char[]) val.get(s);

				if (string.length == s.length())
					return string;
				else
					return s.toCharArray();
			}
			catch (Throwable t) {
			}
		}

		return s.toCharArray();
	}


	/**
	 * Performs an efficient chop operation (removing the final character of the String)<br />
	 * If called with an empty string it will return an empty string.
	 * 
	 * @param src
	 * @return
	 * @throws NullPointerException if the String is null
	 */
	public static String chop(final String src) {
		final int length = src.length();

		if (length != 0)
			return src.substring(0, length - 1);
		else
			return "";
	}

	/**
	 * <strong>DO NOT USE THIS UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING!</strong><br />
	 * <strong>WARNING: The String returned by this method <em>will probably be mutable</em></strong>
	 * 
	 * @param src The string as a char[]
	 * @return
	 */
	public static String fastAllocate(char[] src) {
		return fastAllocate(src, 0, src.length);
	}


	/**
	 * Does a fast concatenation into a new buffer
	 * 
	 * @param srcs
	 * @param offsets
	 * @param counts
	 * @return
	 */
	public static String fastConcat(char[][] srcs, int[] offsets, int[] counts) {
		int size = 0;
		for (int count : counts)
			size += count;

		return fastConcat(srcs, offsets, counts, size);
	}


	/**
	 * Does a fast concatenation into a new buffer
	 * 
	 * @param srcs
	 * @param offsets
	 * @param counts
	 * @return
	 */
	public static String fastConcat(char[][] srcs, int[] offsets, int[] counts, int size) {
		char[] string = new char[size];

		int pos = 0;
		for (int i = 0; i < srcs.length; i++) {
			System.arraycopy(srcs[i], offsets[i], string, pos, counts[i]);
			pos += counts[i];
		}

		return fastAllocate(string, 0, string.length);
	}


	public static String fastConcat(char[][] srcs) {
		int size = 0;
		for (char[] src : srcs)
			if (src != null)
				size += src.length;

		return fastConcat(size, srcs);
	}


	public static String fastConcat(String... srcs) {
		int size = 0;
		for (String src : srcs)
			if (src != null)
				size += src.length();

		return fastConcat(size, srcs);
	}


	public static String fastConcat(final int size, final String[] srcs, final int srcLength) {
		final char[] buffer = new char[size];

		int pos = 0;
		int left = size;
		for (int i = 0; i < srcLength; i++) {
			final String s = srcs[i];
			if (s != null) {
				int length = s.length();
				left -= length;

				if (left < 0)
					length += left;

				s.getChars(0, length, buffer, pos);

				pos += length;
			}
		}

		return fastAllocate(buffer, 0, size);
	}


	public static String fastConcat(int size, String... srcs) {
		return fastConcat(size, srcs, srcs.length);
	}

	public static String fastJoin(String delim, int size, Iterable<String> srcs) {
		char[] buffer = new char[size];

		int delimsize = delim.length();

		int pos = 0;
		int left = size;
		for (String src : srcs) {
			if (src == null)
				continue;

			int len = src.length();

			src.getChars(0, len, buffer, pos); // System.arraycopy(src, 0, buffer, pos, len);
			pos += len;
			if (left != len) {
				delim.getChars(0, delimsize, buffer, pos);
				pos += delimsize;
			}
			left -= (len + delimsize);
		}

		return fastAllocate(buffer, 0, size);
	}


	public static String fastJoin(String delim, String... srcs) {
		int size = (delim.length() * srcs.length) - delim.length();

		for (String src : srcs)
			size += src.length();

		return fastJoin(delim, size, Arrays.asList(srcs));
	}


	/**
	 * Joins a number of character arrays together
	 * 
	 * @param delim the delimiter
	 * @param size the size of the resulting String (including delimiters). This must be correct
	 * @param srcs the source values
	 * @return
	 */
	public static String fastJoin_chars(String delim, int size, Iterable<char[]> srcs) {
		char[] buffer = new char[size];

		int delimsize = delim.length();

		int pos = 0;
		int left = size;
		for (char[] src : srcs) {

			int len = src.length;

			System.arraycopy(src, 0, buffer, pos, len);
			pos += len;
			if (left != len) {
				delim.getChars(0, delimsize, buffer, pos);
				pos += delimsize;
			}
			left -= (len + delimsize);
		}

		return fastAllocate(buffer, 0, size);
	}


	public static String fastJoin_strings(final String delim, final Iterable<String> srcs) {
		int size = 0;
		for (String src : srcs)
			size += src.length();

		return fastJoin_strings(delim, size, srcs);
	}


	public static String fastJoin_strings(final String delim, final int size, final Iterable<String> srcs) {
		char[] buffer = new char[size];

		int delimsize = delim.length();

		int pos = 0;
		int left = size;
		for (String src : srcs) {

			int len = src.length();

			src.getChars(0, len, buffer, pos); // System.arraycopy(src, 0, buffer, pos, len);
			pos += len;
			if (left != len) {
				delim.getChars(0, delimsize, buffer, pos);
				pos += delimsize;
			}
			left -= (len + delimsize);
		}

		return fastAllocate(buffer, 0, size);
	}


	public static String fastJoin_strings(String delim, int size, String... srcs) {
		return fastJoin_strings(delim, size, Arrays.asList(srcs));
	}


	public static String fastConcat(int size, char[]... srcs) {
		return fastConcatChar(size, Arrays.asList(srcs));
	}


	public static String fastConcatString(int size, Iterable<String> srcs) {
		char[] buffer = new char[size];

		int pos = 0;
		int left = size;
		for (String s : srcs) {
			if (s != null) {
				int length = s.length();
				left -= length;

				if (left < 0)
					length += left;

				s.getChars(0, length, buffer, pos);

				pos += length;
			}
		}

		return fastAllocate(buffer, 0, size);
	}


	public static String fastConcatChar(int size, Iterable<char[]> srcs) {
		assert (size > -1);
		assert (srcs != null);

		char[] string = new char[size];

		int pos = 0;
		int left = size;
		for (char[] src : srcs) {
			if (src != null) {
				int length = src.length;
				left -= length;

				if (left < 0)
					length += left;

				System.arraycopy(src, 0, string, pos, length);
				pos += length;
			}
		}

		return fastAllocate(string, 0, size);
	}


	/**
	 * Returns an object which may be used to join a collection of string-returning objects at some later date
	 * 
	 * @param delim
	 * @param srcs
	 * @return
	 */
	public static Callable<String> fastReducingJoin(final String delim, final Callable<String>... srcs) {
		return fastReducingJoin(delim, Arrays.asList(srcs));
	}


	/**
	 * Returns an object which may be used to join a collection of string-returning objects at some later date
	 * 
	 * @param delim
	 * @param srcs
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static Callable<String> fastReducingJoin(final String delim, final Collection<Callable<String>> srcs) {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				String[] srclist = new String[srcs.size()];

				int i = 0;
				for (Callable<String> src : srcs) {
					if (src != null) {
						srclist[i++] = src.call();
					}
				}

				return fastJoin(delim, srclist);
			}
		};
	}
}
