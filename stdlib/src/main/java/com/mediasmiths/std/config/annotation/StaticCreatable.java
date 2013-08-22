package com.mediasmiths.std.config.annotation;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * Contains a mapping of classes and the implied value of a Configurable annotation on them. This allows for Java types (or external library types) to be treated as if they have a Configurable annotation<br />
 * These entries are only used as a fallback and <strong>will not</strong> override any Configurable annotation which is explicitly placed on a class
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class StaticCreatable {
	/**
	 * A Map containing a list of classes and the implied value of a Configurable annotation on them. This allows for Java types (or external library types) to be treated as if they have a Configurable annotation
	 */
	private static final Map<Class, Class> ENTRIES = new HashMap<Class, Class>();


	private StaticCreatable() {

	}


	public static boolean has(final Class c) {
		return ENTRIES.containsKey(c);
	}


	public static Class get(final Class c) {
		return ENTRIES.get(c);
	}


	public static void register(final Class c, final Class configurableType) {
		if (!has(c)) {
			try {
				c.getConstructor(configurableType);
			}
			catch (final Throwable t) {
				final Logger log = Logger.getLogger(StaticCreatable.class);

				log.warn("[StaticCreatable] {register} Registering a StaticCreatable but there's no constructor on " + c.getName() +
						" which takes a single " + configurableType.getName());
			}

			ENTRIES.put(c, configurableType);
		}
		else {
			if (!get(c).equals(configurableType))
				throw new IllegalStateException("Already registered a StaticCreatable for " + c.getName() + " of type " +
						get(c).getName() + ", so cannot register another of type " + configurableType.getName());
		}
	}
}
