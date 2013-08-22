package com.mediasmiths.std.config;

import java.io.File;
import java.util.Map;

import com.mediasmiths.std.config.Configuration;
import com.mediasmiths.std.config.values.xml.DOMValueProvider;
import com.mediasmiths.std.xstream.XSHelper;

public class MapConfigFile {
	public Map<String, Integer> map;


	public static void main(String[] args) throws Exception {
		DOMValueProvider val = new DOMValueProvider(new File("/Users/peter/map.xml"));

		MapConfigFile config = Configuration.get(MapConfigFile.class, val);

		System.out.println(XSHelper.create().serialise(config));
	}
}
