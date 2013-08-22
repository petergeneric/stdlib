package com.mediasmiths.std.config.parser.impl;

import java.util.*;

@SuppressWarnings("rawtypes")
public abstract class AbstractClassToStringParser extends AbstractToStringParser {
	private Set<Class> classes = new HashSet<Class>();


	public AbstractClassToStringParser(Class clazz, Class... classes) {
		this.classes.add(clazz);
		Collections.addAll(this.classes, classes);
	}


	@Override
	public final boolean canParse(Class c) {
		return classes.contains(c);
	}
}
