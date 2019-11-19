package com.peterphi.usermanager.util;

public final class UserManagerBearerToken
{
	public static final String PREFIX = "UMB/";
	public static final String PREFIX_DELEGATED = PREFIX + "DTK/";
	public static final String PREFIX_SERVICE_TOKEN = PREFIX + "STK/";
	private static final String HTTP_AUTHORIZATION_HEADER_BEARER_CONST = "Bearer ";
	private static final String HTTP_AUTHORIZATION_HEADER_PREFIX = HTTP_AUTHORIZATION_HEADER_BEARER_CONST + PREFIX;


	public static boolean isUserManagerBearer(final String token)
	{
		return token.startsWith(PREFIX);
	}


	public static boolean isUserManagerDelegatedBearer(final String token)
	{
		return token.startsWith(PREFIX_DELEGATED);
	}


	public static boolean isUserManagerServiceBearer(final String token)
	{
		return token.startsWith(PREFIX_SERVICE_TOKEN);
	}


	public static boolean isUserManagerBearerAuthorizationHeader(final String header)
	{
		return header != null && header.startsWith(HTTP_AUTHORIZATION_HEADER_PREFIX);
	}


	public static String getTokenFromBearerAuthorizationHeader(final String header)
	{
		assert (isUserManagerBearerAuthorizationHeader(header));

		return header.substring(HTTP_AUTHORIZATION_HEADER_BEARER_CONST.length());
	}
}
