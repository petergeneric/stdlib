package com.mediasmiths.std.config.parser.impl;

import java.io.*;
import com.mediasmiths.std.config.util.ThreadConfigurationFileContext;

@SuppressWarnings("rawtypes")
public class FileParser extends AbstractClassToStringParser {

	public FileParser() {
		super(File.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		if (val.length() != 0) {
			final File file = new File(val);

			// Relative files are permitted. If a relative filename is used then it will be relative to the config file's directory

			if (file.isAbsolute())
				return file;
			else {
				File currentConfigFile = ThreadConfigurationFileContext.peek();

				if (currentConfigFile == null)
					throw new RuntimeException("Cannot parse relative file " + val +
							" because current configuration file cannot be determined");

				return new File(currentConfigFile.getParentFile(), val);
			}

		}
		else {
			return null;
		}
	}

}
