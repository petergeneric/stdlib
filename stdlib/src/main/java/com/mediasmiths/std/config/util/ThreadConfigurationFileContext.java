package com.mediasmiths.std.config.util;

import java.util.*;
import java.io.*;

/**
 * Holds the stack of configuration files being processed by this Thread
 */
public class ThreadConfigurationFileContext {
	private static final ThreadLocal<Stack<File>> stacks = new ThreadLocal<Stack<File>>();


	private ThreadConfigurationFileContext() {
	}


	public static void push(File file) {
		Stack<File> stack = stacks.get();

		if (stack == null) {
			stack = new Stack<File>();
			stacks.set(stack);
		}

		stack.push(file);
	}


	public static void pop(File file) {
		if (peek() == file) {
			Stack<File> stack = stacks.get();

			stack.pop();

			// once the stack is empty, throw it away
			if (stack.isEmpty())
				stacks.remove();
		}
	}


	public static void popAll() {
		stacks.remove();
	}


	public static File peek() {
		Stack<File> stack = stacks.get();

		if (stack == null)
			throw new IllegalStateException("Cannot peek at configuration file stack: no configuration under way!");
		else
			return stack.peek();
	}
}
