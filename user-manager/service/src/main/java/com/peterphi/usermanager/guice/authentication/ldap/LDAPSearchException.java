package com.peterphi.usermanager.guice.authentication.ldap;

public class LDAPSearchException extends RuntimeException
{
	public LDAPSearchException()
	{
		super();
	}


	public LDAPSearchException(String msg)
	{
		super(msg);
	}


	public LDAPSearchException(Throwable cause)
	{
		super(cause);
	}


	public LDAPSearchException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
