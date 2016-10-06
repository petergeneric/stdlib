package com.peterphi.std.azure.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.microsoft.azure.Azure;
import com.microsoft.azure.CloudException;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;

/**
 * Created by bmcleod on 05/09/2016.
 */
public class AzureProvider implements Provider<Azure>
{
	@Inject(optional = true)
	@Named("azure.http.logging.level")
	String logLevelStr = HttpLoggingInterceptor.Level.BASIC.name();

	@Inject
	ServiceClientCredentials credentials;


	@Override
	public Azure get()
	{
		try
		{
			return Azure.configure().withLogLevel(getLogLevel(logLevelStr)).authenticate(credentials).withDefaultSubscription();
		}
		catch (CloudException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	private HttpLoggingInterceptor.Level getLogLevel(final String logLevelStr)
	{
		return HttpLoggingInterceptor.Level.valueOf(logLevelStr);
	}
}
