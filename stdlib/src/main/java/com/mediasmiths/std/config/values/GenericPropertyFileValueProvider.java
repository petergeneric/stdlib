package com.mediasmiths.std.config.values;

import java.lang.reflect.Field;

import com.mediasmiths.std.config.IContextValueProvider;

/**
 * 
 */
public abstract class GenericPropertyFileValueProvider implements IContextValueProvider {
	private final ContextTracker path = new ContextTracker();


	@Override
	public boolean supportsListAutoBounding() {
		return false;
	}


	@Override
	public String get(String defaultValue) {
		return getProperty(path.get(), defaultValue);
	}


	@Override
	public String get(String field, String defaultValue) {
		path.push(field);
		try {
			return get(defaultValue);
		}
		finally {
			path.pop();
		}
	}


	@Override
	public void popContext(Field f) {
		path.pop();
	}


	@Override
	public void pushContext(Field f) {
		if (f == null)
			throw new IllegalArgumentException("Cannot push a null context!");

		path.push(f.getName());
	}


	@Override
	public void pushContext(String fieldName) {
		if (fieldName == null)
			throw new IllegalArgumentException("Cannot push a null context!");

		path.push(fieldName);
	}


	@Override
	public void popContext(String fieldName) {
		path.pop();
	}


	@Override
	public void setContextSubscript(int i) {
		path.setSubscript(i);
	}


	/**
	 * @see com.mediasmiths.std.config.IContextValueProvider#getContextForErrorLog()
	 */
	@Override
	public String getContextForErrorLog() {
		return path.get();
	}


	@Override
	public String getContextForErrorLog(String field) {
		path.push(field);
		try {
			return path.get();
		}
		finally {
			path.pop();
		}
	}


	protected abstract String getProperty(String name, String defaultValue);

}
