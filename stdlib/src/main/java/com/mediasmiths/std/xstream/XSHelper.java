package com.mediasmiths.std.xstream;

import java.io.*;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ToStringConverter;
import com.thoughtworks.xstream.io.*;
import com.thoughtworks.xstream.io.xml.*;

import com.mediasmiths.std.types.*;
import com.mediasmiths.std.xstream.serialisers.InetAddressConverter;

/**
 * A light abstraction layer over XStream, allowing the user to ignore some implementation-specific aspects
 * 
 * 
 */
@SuppressWarnings({"unchecked"})
public class XSHelper extends XStream {
	private static final Logger log = Logger.getLogger(XSHelper.class);

	public boolean hideErrors = true;

	/**
	 * The current object graph serialisation mode (xpath relative seems to be the default for xstream). XPATH_RELATIVE_REFERENCES by default
	 */
	private int currentMode = XStream.XPATH_RELATIVE_REFERENCES;

	/**
	 * Whether XML output will be neatly indented. Enabled by default.
	 */
	private boolean prettyprint = true;

	private static final boolean hasXpp3;
	static {
		final String XPP3_CLASS = "org.xmlpull.v1.XmlPullParser";

		boolean discovered;
		try {
			Class.forName(XPP3_CLASS);

			discovered = true;
		}
		catch (Throwable t) {
			log.info("[XSHelper] {hasXPP3} This installation does not appear to have XPP3 class " + XPP3_CLASS);
			discovered = false;
		}

		hasXpp3 = discovered;
	}


	/**
	 * Determines if XPP3 is available, caching the results of the first query
	 * 
	 * @return True if XPP3 is available, otherwise false
	 */
	public static boolean hasXPP3() {
		return XSHelper.hasXpp3;
	}


	/**
	 * Creates a new instance of the XSHelper class, using the most efficient underlying implementations possible
	 * 
	 * @return
	 */
	public static XSHelper create() {
		XSHelper xs;
		if (hasXPP3())
			xs = new XSHelper();
		else
			xs = new XSHelper(new DomDriver());

		try {
			xs.registerConverter(new ToStringConverter(Version.class));
			xs.registerConverter(new InetAddressConverter());
		}
		catch (NoSuchMethodException e) {
			// ignore
		}

		return xs;
	}


	public static XSHelper create(HierarchicalStreamDriver drv) {
		return new XSHelper(drv);
	}


	/**
	 * Creates a new XStream instance (users should always use .create())
	 */
	private XSHelper() {
		super();
	}


	/**
	 * Creates a new XStream instance (users should always use .create() or .create(HierarchicalStreamDriver)
	 * 
	 * @param drv
	 */
	private XSHelper(HierarchicalStreamDriver drv) {
		super(drv);
	}


	public void parseAnnotations(Class<?>... classes) {
		super.processAnnotations(classes);
	}


	/**
	 * Serialises an object into an OutputStream
	 * 
	 * @param os The OutputStream to use
	 * @param o The object to serialise
	 * @return True if the serialisation succeeded
	 */
	public BooleanMessage serialise(OutputStream os, Object o) {
		try {
			// this.toXML(o, os);
			marshal(o, getWriter(os));

			return new BooleanMessage(true);
		}
		catch (Error e) {
			log.error("[XSHelper] {serialise} Unexpected Error while serialising " + o + ": " + e.getMessage(), e);
			return new BooleanMessage(false, e.getMessage());
		}
	}


	/**
	 * Serialises an object into an OutputStream
	 * 
	 * @param os The Writer to use
	 * @param o The object to serialise
	 * @return True if the serialisation succeeded
	 */
	public BooleanMessage serialise(Writer os, Object o) {
		try {
			marshal(o, getWriter(os));

			return new BooleanMessage(true);
		}
		catch (Error e) {
			log.error("[XSHelper] {serialise} Unexpected Error while serialising " + o + ": " + e.getMessage(), e);
			return new BooleanMessage(false, e.getMessage());
		}
	}


	private HierarchicalStreamWriter getWriter(Writer w) {
		if (prettyprint)
			return new PrettyPrintWriter(w);
		else
			return new CompactWriter(w);
	}


	private HierarchicalStreamWriter getWriter(OutputStream os) {
		return getWriter(new OutputStreamWriter(os));
	}


	private HierarchicalStreamWriter getWriter(File f) throws IOException {
		return getWriter(new FileWriter(f));
	}


	/**
	 * Serialises an object into an OutputStream
	 * 
	 * @param os The OutputStream to use
	 * @param o The object to serialise
	 * @return True if the serialisation succeeded
	 */
	public BooleanMessage serialise(File f, Object o) {
		try {
			marshal(o, getWriter(f));

			return new BooleanMessage(true);
		}
		catch (IOException e) {
			log.error("[XSHelper] {serialise} Unexpected error while opening file writer: " + e.getMessage(), e);
			return new BooleanMessage(false, e.getMessage());
		}
	}


	/**
	 * Serialises an object into a String
	 * 
	 * @param o The object to serialise
	 * @return The serialised object, or null if an exception occurred. Any exceptions are logged.
	 */
	public String serialise(Object o) {
		try {
			StringWriter sw = new StringWriter();
			{
				marshal(o, getWriter(sw));
			}
			return sw.toString();
		}
		catch (Throwable e) {
			log.error("[XSHelper] {serialise} Error while serialising " + o + " " + e.getMessage(), e);

			if (hideErrors) {
				return null;
			}
			else {
				throw new Error("Serialisation failed", e);
			}
		}
	}


	/**
	 * Deserialises an Object from a File.
	 * 
	 * @param f The file to deserialise
	 * @return The deserialised object, or null if an exception occurred. All exceptions are logged.
	 */
	public <T> T deserialise(File f) {
		try {
			return (T) this.fromXML(new FileReader(f));
		}
		catch (Exception e) {
			log.error("[XSHelper] {deserialise} Error while deserialising " + f + ": " + e.getMessage(), e);

			if (hideErrors) {
				return null;
			}
			else {
				throw new Error("Deserialisation failed", e);
			}
		}
	}


	/**
	 * Deserialises a String into an Object
	 * 
	 * @param xml The XML document to deserialise
	 * @return The object (or null if an exception occurred). All exceptions are logged.
	 */
	public <T> T deserialise(String xml) {
		try {
			return (T) this.fromXML(xml);
		}
		catch (Exception e) {
			log.error("[XSHelper] {deserialise} Error while deserialising a String: " + e.getMessage(), e);

			if (hideErrors) {
				return null;
			}
			else {
				throw new Error("Deserialisation failed", e);
			}
		}
	}


	/**
	 * Deserialise an XML document from an InputStream into an Object
	 * 
	 * @param stream The InputStream
	 * @return The Object (or null if an exception occurred). All exceptions are logged
	 */
	public <T> T deserialise(InputStream stream) {
		try {
			return (T) this.fromXML(stream);
		}
		catch (Exception e) {
			log.error("[XSHelper] {deserialise} Error while deserialising " + stream + ": " + e.getMessage(), e);

			if (hideErrors) {
				return null;
			}
			else {
				throw new Error("Deserialisation failed", e);
			}
		}
	}


	/**
	 * Deserialise an XML document from an InputStream into an Object
	 * 
	 * @param stream The Reader
	 * @return The Object (or null if an exception occurred). All exceptions are logged
	 */
	public <T> T deserialise(Reader stream) {
		try {
			return (T) this.fromXML(stream);
		}
		catch (Exception e) {
			log.error("[XSHelper] {deserialise} Error while deserialising " + stream + ": " + e.getMessage(), e);

			if (hideErrors) {
				return null;
			}
			else {
				throw new Error("Deserialisation failed", e);
			}
		}
	}


	/**
	 * Performs a deep clone using serialisation; this is equivalent to <code>(MyType) this.deserialise(this.serialise(obj))</code><br />
	 * Be careful when using this to clone classes you do not own: they may refer to complex/system datastructures with unintended consequences<br />
	 * 
	 * @param <T> some class
	 * @param obj some object
	 * @return a very deep clone of that object
	 */
	public synchronized <T> T clone(T obj) {
		// Temporarily enable ID_REFERENCES (since clone should definitely work with graphs)
		final int mode = this.getMode();
		final boolean oldprettyprint = this.getPrettyPrint();

		try {
			this.setMode(XStream.ID_REFERENCES);
			this.setPrettyPrint(false);

			return (T) this.deserialise(this.serialise(obj));
		}
		finally {
			this.setMode(mode);
			this.setPrettyPrint(oldprettyprint);
		}
	}


	public void setNoObjectGraph(boolean disable) {
		if (disable)
			disableGraph();
		else
			enableGraph();
	}


	/**
	 * @return a copy of this (for chaining)
	 */
	public XSHelper enableGraph() {
		this.setMode(XStream.XPATH_RELATIVE_REFERENCES);

		return this;
	}


	/**
	 * @return a copy of this (for chaining)
	 */
	public XSHelper disableGraph() {
		this.setMode(XStream.NO_REFERENCES);

		return this;
	}


	/**
	 * Sets the current graphing mode, logging its value for use in this class.<br />
	 * Possible values are: <code>XStream.NO_REFERENCES, XStream.XPATH_RELATIVE_REFERENCES, XStream.XPATH_ABSOLUTE_REFERENCES, XStream.ID_REFERENCES</code>
	 */
	@Override
	public void setMode(int mode) {
		this.currentMode = mode;
		super.setMode(mode);
	}


	public boolean getPrettyPrint() {
		return this.prettyprint;
	}


	/**
	 * Enables/Disables pretty printing (indented XML output)
	 * 
	 * @param value true for pretty printing, false for compact output
	 * @return a copy of <code>this</code> (for chaining)
	 */
	public XSHelper setPrettyPrint(boolean value) {
		this.prettyprint = value;

		return this;
	}


	/**
	 * Returns the current graphing mode
	 * 
	 * @return
	 */
	public int getMode() {
		return currentMode;
	}
}
