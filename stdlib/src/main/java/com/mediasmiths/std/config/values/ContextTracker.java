package com.mediasmiths.std.config.values;

import java.util.*;

/**
 * Keeps track of the hierarchical dot-separated context, including handling list indexes, etc
 */
public class ContextTracker {
	public Stack<String> context = new Stack<String>();


	public ContextTracker() {
		context.push("");
	}


	public void push(String name) {
		final String newContext;
		{
			final String current = get();
			if (current.isEmpty())
				newContext = name;
			else
				newContext = current + "." + name;
		}

		context.push(newContext);
	}


	public String pop() {
		return context.pop();
	}


	public void setSubscript(int i) {
		final String context = this.context.pop();

		String newContext;
		{
			if (context.endsWith("]")) // Remove current subscript
				newContext = context.substring(0, context.lastIndexOf("["));
			else
				newContext = context;

			// Add the subscript to the path
			newContext += "[" + Integer.toString(i) + "]";
		}

		this.context.push(newContext);
	}


	public String get() {
		return context.peek();
	}

}
