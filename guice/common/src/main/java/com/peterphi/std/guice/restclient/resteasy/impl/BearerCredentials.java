package com.peterphi.std.guice.restclient.resteasy.impl;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import java.util.function.Supplier;

class BearerCredentials implements Credentials, Serializable
{
	private static final long serialVersionUID = 243343858802739400L;

	private final BasicUserPrincipal principal = new BasicUserPrincipal("BearerToken");
	private final Supplier<String> supplier;
	private final String fixedValue;


	public BearerCredentials(final Supplier<String> supplier)
	{
		Objects.requireNonNull(supplier, "Must provide a non-null supplier!");

		this.supplier = supplier;
		this.fixedValue = null;
	}


	public BearerCredentials(final String fixedValue)
	{
		Objects.requireNonNull(fixedValue, "Must provide a non-null fixed value!");

		this.supplier = null;
		this.fixedValue = fixedValue;
	}


	@Override
	public Principal getUserPrincipal()
	{
		return this.principal;
	}


	public String getUserName()
	{
		return this.principal.getName();
	}


	@Override
	public String getPassword()
	{
		return null;
	}


	public String getToken()
	{
		if (fixedValue != null)
			return fixedValue;
		else
			return supplier.get();
	}
}

