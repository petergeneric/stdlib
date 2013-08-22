package com.mediasmiths.std.config.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@SuppressWarnings({ "unchecked" })
public class TypeAndClass<T> {
	public Type type;
	public Class<T> clazz;


	public TypeAndClass(Class<T> clazz) {
		this(clazz, null);
	}


	public TypeAndClass(Class<T> clazz, final Type type) {
		this.clazz = clazz;
		if (type != null)
			this.type = type;
		else
			this.type = clazz;
	}


	public TypeAndClass(Field field) {
		this((Class<T>) field.getType(), field.getGenericType());
	}


	public TypeAndClass(Method method) {
		this((Class<T>) method.getReturnType(), method.getGenericReturnType());
	}

}
