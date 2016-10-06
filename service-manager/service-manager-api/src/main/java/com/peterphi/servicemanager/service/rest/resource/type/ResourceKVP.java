package com.peterphi.servicemanager.service.rest.resource.type;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlType(name = "KVPType")
public class ResourceKVP
{
	@XmlAttribute(name = "name", required = true)
	public String name;
	@XmlValue
	public String value;


	public ResourceKVP()
	{
	}


	public ResourceKVP(final String name, final String value)
	{
		this.name = name;
		this.value = value;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("name", name).add("value", value).toString();
	}


	public static Map<String, String> toMap(final List<ResourceKVP> metadata)
	{
		Map<String, String> map = new HashMap<>();

		for (ResourceKVP kvp : metadata)
		{
			map.put(kvp.name, kvp.value);
		}

		return map;
	}


	public static List<ResourceKVP> fromMap(final Map<String, String> metadata)
	{
		List<ResourceKVP> list = new ArrayList<>();

		for (Map.Entry<String, String> entry : metadata.entrySet())
			list.add(new ResourceKVP(entry.getKey(), entry.getValue()));

		return list;
	}
}
