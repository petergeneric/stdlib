package com.peterphi.std.guice.common.converter;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.threading.Timeout;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

public class PropertiesTypeConversionModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		convertToTypes(Matchers.only(TypeLiteral.get(URI.class)), new URITypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(URL.class)), new URLTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(File.class)), new FileTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(InetAddress.class)), new InetAddressTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(Timeout.class)), new TimeoutConverter());
	}
}
