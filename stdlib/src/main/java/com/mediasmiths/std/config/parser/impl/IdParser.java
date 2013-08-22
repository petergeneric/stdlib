package com.mediasmiths.std.config.parser.impl;

import java.lang.reflect.Constructor;
import com.mediasmiths.std.types.*;

@SuppressWarnings({ "rawtypes","unchecked" })
public class IdParser extends AbstractToStringParser {

	@Override
	protected Object parse(Class t, String val) {
		return createId(t, val);
	}


	/**
	 * 
	 * @param t
	 * @param id
	 * @return
	 */
	private static Object createId(Class t, String id) {
		try {
			Constructor constructor = t.getConstructor(String.class);

			return constructor.newInstance(id);
		}
		catch (NoSuchMethodException e) {
			throw new Error("Cannot construct Id of type " + t + ": no String constructor!", e);
		}
		catch (Throwable e) {
			throw new Error("Cannot construct Id of type " + t + ": " + e.getMessage(), e);
		}
	}


	@Override
	public boolean canParse(Class c) {
		return c.getSuperclass() == Id.class;
	}
}
