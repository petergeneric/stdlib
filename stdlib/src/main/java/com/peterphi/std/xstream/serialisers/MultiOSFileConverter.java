package com.peterphi.std.xstream.serialisers;

import java.io.File;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

@SuppressWarnings({ "rawtypes" })
public class MultiOSFileConverter extends AbstractSingleValueConverter {
	@Override
	public Object fromString(String s) {
		String mS = org.apache.commons.io.FilenameUtils.separatorsToSystem(s);

		return new File(mS);
	}


	@Override
	public String toString(Object obj) {
		return ((File) obj).toString();
	}


	@Override
	public boolean canConvert(Class type) {
		return type.equals(File.class);
	}

}
