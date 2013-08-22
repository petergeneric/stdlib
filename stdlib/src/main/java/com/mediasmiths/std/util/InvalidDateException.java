package com.mediasmiths.std.util;

// InvalidDateException.java
// Id: InvalidDateException.java,v 1.1 2000/09/26 13:54:04 bmahe Exp
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

/**
 * @version $Revision$
 * @author Benot Mah (bmahe@w3.org) Moved to com.mediasmiths.std.util for ease of use from http://dev.w3.org/cvsweb/~checkout~/java/classes/org/w3c/util/DateParser.java?rev=1.5&content-type=text/plain
 */
public class InvalidDateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidDateException(String msg) {
		super(msg);
	}

}
