package com.peterphi.servicemanager.hostagent.webapp.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.io.FileHelper;
import com.peterphi.std.system.exec.Exec;
import com.peterphi.std.system.exec.Execed;
import com.peterphi.std.threading.Timeout;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NginxService
{
	@Inject(optional = true)
	@Named("host-agent.binpath")
	public File binPath = new File("/opt/host-agent/bin");


	/**
	 * Reload the nginx configuration
	 */
	public void reload()
	{
		try
		{
			final Execed process = Exec.rootUtility(new File(binPath, "nginx-reload").getAbsolutePath());
			process.waitForExit(new Timeout(30, TimeUnit.SECONDS).start(), 0);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error executing nginx-reload command", e);
		}
	}


	/**
	 * Rewrite the nginx site configuration and reload
	 *
	 * @param config
	 * 		the nginx site configuration
	 */
	public void reconfigure(final String config)
	{
		try
		{
			final File tempFile = File.createTempFile("nginx", ".conf");
			try
			{
				FileHelper.write(tempFile, config);

				final Execed process = Exec.rootUtility(new File(binPath, "nginx-reconfigure").getAbsolutePath(),
				                                        tempFile.getAbsolutePath());

				process.waitForExit(new Timeout(30, TimeUnit.SECONDS).start(), 0);
			}
			finally
			{
				FileUtils.deleteQuietly(tempFile);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error executing nginx-reload command", e);
		}

		reload();
	}


	/**
	 * Install new SSL Certificates for the host
	 *
	 * @param key
	 * @param cert
	 * @param chain
	 */
	public void installCertificates(final String key, final String cert, final String chain)
	{
		try
		{
			final File keyFile = File.createTempFile("key", ".pem");
			final File certFile = File.createTempFile("cert", ".pem");
			final File chainFile = File.createTempFile("chain", ".pem");

			try
			{
				FileHelper.write(keyFile, key);
				FileHelper.write(certFile, cert);
				FileHelper.write(chainFile, chain);

				final Execed process = Exec.rootUtility(new File(binPath, "cert-update").getAbsolutePath(),
				                                        keyFile.getAbsolutePath(),
				                                        certFile.getAbsolutePath(),
				                                        chainFile.getAbsolutePath());

				process.waitForExit(new Timeout(30, TimeUnit.SECONDS).start(), 0);
			}
			finally
			{
				FileUtils.deleteQuietly(keyFile);
				FileUtils.deleteQuietly(certFile);
				FileUtils.deleteQuietly(chainFile);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error executing cert-update command", e);
		}
	}
}
