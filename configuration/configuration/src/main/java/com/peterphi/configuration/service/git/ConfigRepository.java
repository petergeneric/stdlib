package com.peterphi.configuration.service.git;


import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigRepository
{
	private final Repository repo;


	public ConfigRepository(final Repository repo)
	{
		this.repo = repo;
	}


	public ConfigPropertyData get(String ref, String path)
	{
		return RepoHelper.read(repo, ref, path);
	}


	public List<String> getPaths(String ref)
	{
		try
		{
			return RepoHelper.parseCommit(repo, RepoHelper.resolve(repo, ref))
			                 .keySet()
			                 .stream()
			                 .sorted()
			                 .collect(Collectors.toList());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error parsing commit", e);
		}
	}


	public Map<String, Map<String, ConfigPropertyValue>> getAll(String ref)
	{
		try
		{
			return RepoHelper.parseCommit(repo, RepoHelper.resolve(repo, ref));
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read from repo!", e);
		}
	}


	/**
	 * Create a new commit reflecting the provided properties, removing any property not mentioned here
	 *
	 * @param name
	 * @param email
	 * @param data
	 * @param message
	 */
	public void setAll(final String name,
	                   final String email,
	                   final Map<String, Map<String, ConfigPropertyValue>> data,
	                   final String message)
	{
		set(name, email, data, true, message);
	}


	/**
	 * Create a new commit reflecting the provided properties
	 *
	 * @param name
	 * @param email
	 * @param data
	 * @param erase
	 * 		if true all existing properties will be erased
	 * @param message
	 */
	public void set(final String name,
	                final String email,
	                final Map<String, Map<String, ConfigPropertyValue>> data,
	                final boolean erase,
	                final String message)
	{
		try
		{
			RepoHelper.write(repo, name, email, data, erase, message);
		}
		catch (Exception e)
		{
			try
			{
				RepoHelper.reset(repo);
			}
			catch (Exception ee)
			{
				throw new RuntimeException("Error writing updated repository, then could not reset work tree", e);
			}

			throw new RuntimeException("Error writing updated repository, work tree reset", e);
		}
	}
}
