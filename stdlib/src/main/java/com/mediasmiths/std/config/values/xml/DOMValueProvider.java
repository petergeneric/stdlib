package com.mediasmiths.std.config.values.xml;

import java.util.*;
import java.io.*;
import java.lang.reflect.Field;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import com.mediasmiths.std.config.IContextValueProvider;
import com.mediasmiths.std.config.values.NoArrayException;
import com.mediasmiths.std.io.FileHelper;

public class DOMValueProvider implements IContextValueProvider {
	private final XMLElement root;
	private final Stack<XMLElementRef> stack = new Stack<XMLElementRef>();


	private static XMLElement parseXML(InputStream is) throws RuntimeException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			factory.setNamespaceAware(true);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(false);
			factory.setCoalescing(false);

			DocumentBuilder builder = factory.newDocumentBuilder();

			Document document = builder.parse(is);
			XMLElement.stripWhitespace(document);

			return new XMLElement(document);
		}
		catch (Throwable e) {
			throw new RuntimeException("Failed to parse XML document: " + e.getMessage(), e);
		}
	}


	private static XMLElement parseXML(final Reader reader) throws RuntimeException {
		try {
			final ByteArrayInputStream bis;
			{
				final String contents = FileHelper.cat(reader);
				bis = new ByteArrayInputStream(contents.getBytes());
			}

			try {
				return parseXML(bis);
			}
			finally {
				bis.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Unexpected IOException: " + e.getMessage(), e);
		}
	}


	private static XMLElement parseXML(File file) throws RuntimeException {
		try {
			FileInputStream is = new FileInputStream(file);
			try {
				return parseXML(is);
			}
			finally {
				is.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Unexpected IO exception: " + e.getMessage(), e);
		}
	}


	public DOMValueProvider(File file) throws RuntimeException {
		this(parseXML(file));
	}


	public DOMValueProvider(InputStream stream) throws RuntimeException {
		this(parseXML(stream));
	}


	public DOMValueProvider(Reader reader) throws RuntimeException {
		this(parseXML(reader));
	}


	public DOMValueProvider(XMLElement doc) {
		this.root = doc;
	}


	@Override
	public String get(final String defaultValue) {
		final XMLElement obj = stack.peek().get();

		if (obj != null)
			return obj.getValue();
		else
			return defaultValue;
	}


	@Override
	public String get(String field, String defaultValue) {
		pushContext(field);
		try {
			return get(defaultValue);
		}
		finally {
			popContext(field);
		}
	}


	@Override
	public void pushContext(Field f) {
		pushContext(f.getName());
	}


	@Override
	public void popContext(Field f) {
		popContext(f.getName());
	}


	@Override
	public void pushContext(String name) {
		final XMLElementRef parent = (stack.isEmpty()) ? null : stack.peek();
		final XMLElementRef ref = new XMLElementRef(root, parent, name);

		stack.push(ref);
	}


	@Override
	public void popContext(String name) {
		XMLElementRef elementRef = stack.pop();

		if (!elementRef.getName().equals(name))
			throw new IllegalStateException();
	}


	@Override
	public void setContextSubscript(int i) throws NoArrayException {
		final XMLElementRef elementRef = stack.peek();

		boolean valid = elementRef.setSubscript(i);

		if (!valid)
			throw new IndexOutOfBoundsException(elementRef + ": does not have subscript " + i);
	}


	@Override
	public String getContextForErrorLog(String field) {
		pushContext(field);
		try {
			return getContextForErrorLog();
		}
		finally {
			popContext(field);
		}
	}


	@Override
	public String getContextForErrorLog() {
		return stack.peek().toString();
	}


	@Override
	public boolean supportsListAutoBounding() {
		return true; // throws IndexOutOfBoundsException when setSubscript() is called with an index which is too large
	}
}
