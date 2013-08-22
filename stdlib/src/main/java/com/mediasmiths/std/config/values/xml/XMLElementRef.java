package com.mediasmiths.std.config.values.xml;

import java.util.*;
import com.mediasmiths.std.config.values.NoArrayException;

class XMLElementRef {
	private final XMLElement root;
	private final XMLElementRef parent;
	private final String name;
	private Integer subscript;

	private transient List<XMLElement> _cached;
	private transient boolean _isCached = false;


	public XMLElementRef(final XMLElement root, XMLElementRef parent, String name) {
		this(root, parent, name, null);
	}


	private XMLElementRef(final XMLElement root, final XMLElementRef parent, String name, Integer subscript) {
		this.root = root;
		this.parent = parent;
		this.name = name;
		this.subscript = subscript;
	}


	public boolean setSubscript(int subscript) throws NoArrayException{
		// Make sure there's a valid for this subscript
		final List<XMLElement> array = getArray();

		if (array == null)
			throw new NoArrayException("No array found at " + this);
		else {
			final boolean withinRange = array.size() > subscript;

			// Apply the new subscript if it's valid
			if (withinRange)
				this.subscript = subscript;

			return withinRange;
		}
	}


	public XMLElement get() {
		if (subscript == null)
			return getOne();
		else {
			final List<XMLElement> array = getArray();

			if (array == null)
				throw new IllegalStateException("Should never have a null list returned from getArray!");
			else
				return array.get(subscript);
		}
	}


	private XMLElement getParentElement() {
		if (parent != null) {
			return parent.get();
		}
		else
			return root;
	}


	public List<XMLElement> getElement() {
		if (!_isCached) {
			_cached = doGetElement();
			_isCached = true;
		}

		return _cached;
	}


	private List<XMLElement> doGetElement() {
		final XMLElement parentElement = getParentElement();

		// If there's no parent, return null immediately
		if (parentElement == null)
			return null;
		else {
			final List<XMLElement> matches = parentElement.getChildren(name);

			if (matches == null)
				return null;
			else if (matches.size() == 0)
				return matches;
			else if (matches.size() == 1)
				return matches;
			else
				throw new IllegalArgumentException("Multiple XML elements called " + name + " found for " + this);
		}
	}


	private List<XMLElement> getArray() {
		final List<XMLElement> element = getElement();

		if (element == null || element.size() == 0)
			return null; // array not specified
		else {
			final XMLElement match = element.get(0);

			final List<XMLElement> items = match.getChildren("item");

			if (items != null)
				return items;
			else
				return Collections.emptyList();
		}
	}


	private XMLElement getOne() {
		final List<XMLElement> element = getElement();

		if (element == null || element.isEmpty())
			return null;
		else
			return element.get(0);
	}


	public String getName() {
		return name;
	}


	private String getComponentName() {
		if (subscript == null)
			return name;
		else
			return name + "[" + subscript.toString() + "]";
	}


	@Override
	public String toString() {
		if (parent != null)
			return parent.toString() + "." + getComponentName();
		else
			return getComponentName();
	}
}
