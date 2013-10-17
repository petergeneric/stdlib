package com.peterphi.std.xstream.serialisers;

import java.util.*;
import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.*;

@SuppressWarnings({ "rawtypes","unchecked" })
public class MapStringStringConverter implements Converter {

	@Override
	public boolean canConvert(Class clazz) {
		if (Map.class.isAssignableFrom(clazz)) {
			return true; // can we test if the class is a Map<String, String> ?
		}
		else {
			return false;
		}
	}


	@Override
	public void marshal(Object valueO, HierarchicalStreamWriter writer, MarshallingContext context) {
		Map<String, String> val = (Map<String, String>) valueO;

		for (Map.Entry<String, String> entry : val.entrySet()) {
			writer.startNode(entry.getKey());
			writer.setValue(entry.getValue());
			writer.endNode();
		}
	}


	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Map<String, String> map = new HashMap<String, String>();

		while (reader.hasMoreChildren()) {
			reader.moveDown(); // move down to the Value

			String key = reader.getNodeName();
			String value = reader.getValue();

			map.put(key, value);

			reader.moveUp(); // move back up to the Map
		}

		return map;
	}
}