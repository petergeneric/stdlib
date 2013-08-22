package com.mediasmiths.std.config.parser;

import com.mediasmiths.std.config.IContextValueProvider;

/**
 * The interface for a parser of configuration context fragments
 */
@SuppressWarnings({ "rawtypes" })
public interface IConfigParser<T> {
	/**
	 * Reads a class of exactly type <code>c</code> from the top of the stack
	 * 
	 * @param factory The factory to use to construct new parsers
	 * @param c The type to read from the value provider
	 * @param required True if the field is mandatory, otherwise false
	 * @param values
	 * @return
	 */
	public T read(ParserFactory factory, TypeAndClass<T> c, boolean required, IContextValueProvider values);


	public boolean canParse(Class c);
}
