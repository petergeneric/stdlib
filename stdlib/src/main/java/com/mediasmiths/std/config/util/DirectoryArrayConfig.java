package com.mediasmiths.std.config.util;

import java.util.regex.Pattern;
import java.io.*;
import com.mediasmiths.std.config.annotation.Ignore;
import com.mediasmiths.std.config.annotation.Optional;

public class DirectoryArrayConfig {
	@SuppressWarnings("rawtypes")
	public Class type;

	public File directory;

	@Optional
	public String regex = null;

	@Optional
	public String suffix = null;

	@Optional
	public String prefix = null;

	@Optional
	public int min = 0;

	@Optional
	public int max = Integer.MAX_VALUE;

	@Ignore
	private Pattern _regex = null;


	private Pattern getRegex() {
		if (regex != null && !regex.isEmpty()) {
			if (_regex == null) {
				final int flags = Pattern.CASE_INSENSITIVE;

				_regex = Pattern.compile(regex, flags);
			}

			return _regex;
		}
		else {
			return null;
		}
	}


	public boolean isPermitted(File dir, String name) {
		if (!isPermittedRegex(dir, name))
			return false;

		if (!isPermittedPrefix(dir, name))
			return false;

		if (!isPermittedSuffix(dir, name))
			return false;

		return true;
	}


	private boolean isPermittedSuffix(File dir, String name) {
		if (suffix != null)
			return name.toLowerCase().endsWith(suffix.toLowerCase());
		else
			return true;
	}


	private boolean isPermittedPrefix(File dir, String name) {
		if (prefix != null)
			return name.toLowerCase().startsWith(prefix.toLowerCase());
		else
			return true;
	}


	private boolean isPermittedRegex(File dir, String name) {
		Pattern p = getRegex();

		if (p != null)
			return p.matcher(name).matches();
		else
			return true;

	}
}
