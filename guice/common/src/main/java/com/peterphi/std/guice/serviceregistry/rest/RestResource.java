package com.peterphi.std.guice.serviceregistry.rest;

public class RestResource
{
	private Class<?> clazz;

	public RestResource(Class<?> clazz)
	{
		this.clazz = clazz;
	}

	public Class<?> getResourceClass()
	{
		return clazz;
	}
}
