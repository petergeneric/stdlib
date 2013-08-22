package com.mediasmiths.std.config;

import java.lang.reflect.Field;

public interface IContextValueProvider {
	public String get(String defaultValue);


	public String get(String field, String defaultValue);


	public void pushContext(Field f);


	public void popContext(Field f);


	public void pushContext(String fieldName);


	public void popContext(String fieldName);


	public void setContextSubscript(int i);


	public boolean supportsListAutoBounding();


	/**
	 * Gets the context which represents this field as a child of the top of the stack
	 * 
	 * @return
	 */
	public String getContextForErrorLog(String field);


	/**
	 * Returns the current context
	 * 
	 * @return
	 */
	public String getContextForErrorLog();
}
