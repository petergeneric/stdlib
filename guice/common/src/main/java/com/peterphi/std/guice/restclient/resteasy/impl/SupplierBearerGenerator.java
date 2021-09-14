package com.peterphi.std.guice.restclient.resteasy.impl;

import java.util.function.Supplier;

public class SupplierBearerGenerator implements BearerGenerator
{
	private final Supplier<String> supplier;


	public SupplierBearerGenerator(final Supplier<String> supplier)
	{
		this.supplier = supplier;
	}


	@Override
	public void setDefaultBearerToken(final String token)
	{
	}


	@Override
	public String getToken()
	{
		return supplier.get();
	}
}
