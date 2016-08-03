package com.peterphi.configuration.service.git;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.io.FileHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoHelper
{
	public static final String KEY_INCLUDE = "include";
	public static final String KEY_INCLUDE_SHALLOW = "shallow-include";

	private static Cache<String, Map<String, Map<String, ConfigPropertyValue>>> COMMIT_CACHE = CacheBuilder.newBuilder()
	                                                                                                       .maximumSize(8)
	                                                                                                       .build();


	public static RevCommit resolve(final Repository repo, final String ref) throws IOException
	{
		return new RevWalk(repo).parseCommit(repo.resolve(ref));
	}


	public static ConfigPropertyData read(final Repository repo, final String ref, final String path)
	{
		try
		{
			return read(repo, resolve(repo, ref), path);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read config from repo", e);
		}
	}


	private static ConfigPropertyData read(final Repository repo, final RevCommit commit, final String path) throws IOException
	{
		ConfigPropertyData data = new ConfigPropertyData();
		data.timestamp = new Date(1000L * commit.getCommitTime());

		data.path = normalisePath(path);
		data.revision = commit.getId().getName();
		data.properties = readProperties(repo, commit, path);

		return data;
	}


	private static List<ConfigPropertyValue> readProperties(final Repository repo,
	                                                        final RevCommit commit,
	                                                        final String path) throws IOException
	{
		// Read all properties
		Map<String, Map<String, ConfigPropertyValue>> config = parseCommit(repo, commit);

		// Now extract the properties from the path requested
		final Map<String, ConfigPropertyValue> properties = readProperties(config, path, true);

		return new ArrayList<>(properties.values());
	}


	private static Map<String, ConfigPropertyValue> readProperties(final Map<String, Map<String, ConfigPropertyValue>> config,
	                                                               final String path,
	                                                               final boolean includeParents)
	{
		Map<String, ConfigPropertyValue> properties = new HashMap<>();

		final List<String> paths = includeParents ? splitPath(path) : Collections.singletonList(path);

		for (String pathComponent : paths)
		{
			final Map<String, ConfigPropertyValue> pathComponentProperties = config.get(pathComponent);

			if (pathComponentProperties != null)
			{
				// First pick up hierarchy includes
				if (pathComponentProperties.containsKey(KEY_INCLUDE))
				{
					for (String includePath : getIncludePaths(pathComponentProperties.get(KEY_INCLUDE)))
						properties.putAll(readProperties(config, includePath, true));
				}

				// Next pick up single-path includes
				if (pathComponentProperties.containsKey(KEY_INCLUDE_SHALLOW))
				{
					for (String includePath : getIncludePaths(pathComponentProperties.get(KEY_INCLUDE_SHALLOW)))
						properties.putAll(readProperties(config, includePath, true));
				}

				// Now override with local properties
				properties.putAll(pathComponentProperties);
			}
		}

		return properties;
	}


	static String[] getIncludePaths(final ConfigPropertyValue includeonly)
	{
		return includeonly.value.split("\n");
	}


	public static String normalisePath(String path)
	{
		// Remove double slashes
		path = path.replace("//", "/");

		if (path.startsWith("/")) // Remove leading slash if present
			path = path.substring(1);
		if (path.endsWith("/")) // Remove trailing slash if present
			path = path.substring(0, path.length() - 1);

		return path;
	}


	static List<String> splitPath(String path)
	{
		path = normalisePath(path);

		List<String> hierarchy = new ArrayList<>();
		hierarchy.add(""); // root

		final String[] components = StringUtils.split(path, '/');

		for (int i = 1; i <= components.length; i++)
		{
			hierarchy.add(StringUtils.join(components, '/', 0, i));
		}

		return hierarchy;
	}


	public static void write(final Repository repo,
	                         final String name,
	                         final String email,
	                         final Map<String, Map<String, ConfigPropertyValue>> data,
	                         final ConfigChangeMode changeMode,
	                         final String message)
	{
		final File workTree = repo.getWorkTree();

		if (changeMode == ConfigChangeMode.WIPE_ALL)
		{
			// Remove all existing config
			try
			{
				for (File file : workTree.listFiles())
				{
					if (!StringUtils.startsWithIgnoreCase(file.getName(), ".git"))
						FileUtils.forceDelete(file);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error clearing current work tree!", e);
			}
		}

		// Now write all config
		try
		{
			for (Map.Entry<String, Map<String, ConfigPropertyValue>> entry : data.entrySet())
			{
				final String path = entry.getKey();
				final Map<String, ConfigPropertyValue> props = entry.getValue();

				final File folder = new File(workTree, path.replace('/', File.separatorChar));

				// Make sure the path exists
				FileUtils.forceMkdir(folder);

				if (changeMode == ConfigChangeMode.WIPE_REFERENCED_PATHS)
				{
					File[] files = folder.listFiles((FileFilter) FileFilterUtils.fileFileFilter());

					if (files != null)
						for (File file : files)
							FileUtils.forceDelete(file);
				}

				for (Map.Entry<String, ConfigPropertyValue> propEntry : props.entrySet())
				{
					final File file = new File(folder, propEntry.getKey());
					try
					{
						FileHelper.write(file, propEntry.getValue().value);
					}
					catch (IOException e)
					{
						throw new IOException("Error writing to " + file, e);
					}
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error writing properties to work tree!", e);
		}

		// Commit the changes to the repository
		{
			try
			{
				Git git = new Git(repo);

				// Add all changes
				git.add().addFilepattern(".").call();

				// Now commit
				git.commit().setAll(true).setAuthor(name, email).setMessage(message).call();
			}
			catch (GitAPIException e)
			{
				throw new RuntimeException("Error committing changes!", e);
			}
		}
	}


	public static Map<String, Map<String, ConfigPropertyValue>> parseCommit(final Repository repo,
	                                                                        final RevCommit commit) throws IOException
	{
		final String id = commit.getId().getName();

		Map<String, Map<String, ConfigPropertyValue>> parsed;

		parsed = COMMIT_CACHE.getIfPresent(id);

		if (parsed == null)
		{
			parsed = _parseCommit(repo, commit);
			COMMIT_CACHE.put(id, parsed);
		}

		return parsed;
	}


	private static Map<String, Map<String, ConfigPropertyValue>> _parseCommit(final Repository repo,
	                                                                          final RevCommit commit) throws IOException
	{
		TreeWalk walk = new TreeWalk(repo);

		walk.addTree(commit.getTree());
		walk.setRecursive(false);

		// Map of Path to properties defined at this path
		Map<String, Map<String, ConfigPropertyValue>> all = new HashMap<>();

		all.put("", new HashMap<>());
		while (walk.next())
		{
			if (walk.isSubtree())
			{
				final String path = walk.getPathString();
				all.put(path, new HashMap<>());

				walk.enterSubtree();
			}
			else
			{
				final String path;
				final String propertyName;
				{
					final String pathString = walk.getPathString();
					final int lastSlash = pathString.lastIndexOf('/');

					if (lastSlash == -1)
					{
						path = "";
						propertyName = pathString;
					}
					else
					{
						path = pathString.substring(0, lastSlash);
						propertyName = pathString.substring(lastSlash + 1);
					}
				}

				final byte[] bytes = repo.open(walk.getObjectId(0)).getCachedBytes();

				// Parse the data as a UTF-8 String
				final String str = new String(bytes, StandardCharsets.UTF_8);

				ConfigPropertyValue val = new ConfigPropertyValue(path, propertyName, str);

				all.get(path).put(propertyName, val);
			}
		}

		return all;
	}


	public static void reset(final Repository repo)
	{
		try
		{
			new Git(repo).reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD").call();
		}
		catch (GitAPIException e)
		{
			throw new RuntimeException("Failed to issue git reset HEAD!", e);
		}
	}


	public static void addRemote(final Repository repo, final String remote, final String url)
	{
		try
		{
			// Add the new remote
			{
				final RemoteAddCommand remoteAdd = new Git(repo).remoteAdd();

				remoteAdd.setName(remote);
				remoteAdd.setUri(new URIish(url));

				remoteAdd.call();
			}

			// Set up tracking
			{
				StoredConfig config = repo.getConfig();

				config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", "remote", remote);
				config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", "merge", "refs/heads/master");

				config.save();
			}
		}
		catch (IOException | URISyntaxException | GitAPIException e)
		{
			throw new RuntimeException("Failed to issue git remote add", e);
		}
	}


	public static void pull(final Repository repo, final String remote, final CredentialsProvider credentials)
	{
		try
		{
			new Git(repo).pull().setRemote(remote).setStrategy(MergeStrategy.OURS).setCredentialsProvider(credentials).call();
		}
		catch (GitAPIException e)
		{
			throw new RuntimeException("Failed to issue git pull", e);
		}
	}


	public static void push(final Repository repo, final String remote, final CredentialsProvider credentials)
	{
		try
		{
			new Git(repo).push().setForce(true).setRemote(remote).setCredentialsProvider(credentials).call();
		}
		catch (GitAPIException e)
		{
			throw new RuntimeException("Failed to issue git pull", e);
		}
	}


	public static List<ConfigCommit> log(final Repository repo, final int max)
	{
		try
		{
			Git git = new Git(repo);

			final Iterable<RevCommit> commits = git.log().setMaxCount(max).call();

			List<ConfigCommit> ret = new ArrayList<>();

			for (RevCommit commit : commits)
			{
				final PersonIdent author = commit.getAuthorIdent();
				final Date timestamp = new Date(commit.getCommitTime() * 1000L);

				ret.add(new ConfigCommit(timestamp, author.getName(), author.getEmailAddress(), commit.getFullMessage()));
			}

			return ret;
		}
		catch (GitAPIException e)
		{
			throw new RuntimeException("Error issuing git log", e);
		}
	}
}
