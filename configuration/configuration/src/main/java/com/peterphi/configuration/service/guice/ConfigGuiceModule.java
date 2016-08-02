package com.peterphi.configuration.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigRepository;
import com.peterphi.configuration.service.git.RepoHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class ConfigGuiceModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(ConfigGuiceModule.class);


	@Override
	protected void configure()
	{

	}


	@Provides
	@Singleton
	@Named("config")
	public ConfigRepository getRepository(@Named("repo.config.path") final File workingDirectory,
	                                      @Named("repo.config.remote") final String remote) throws IOException, GitAPIException
	{
		final File gitDir = new File(workingDirectory, ".git");

		final boolean newlyCreated;

		// Create the git repository if it doesn't already exist
		if (!gitDir.exists())
		{
			log.info("Initialising new git dir at: " + workingDirectory);

			FileUtils.forceMkdir(workingDirectory);

			InitCommand init = new InitCommand();
			init.setBare(false).setDirectory(workingDirectory).setGitDir(gitDir).call();

			newlyCreated = true;
		}
		else
		{
			newlyCreated = false;
		}

		FileRepositoryBuilder frb = new FileRepositoryBuilder();
		Repository repo = frb.setGitDir(gitDir).readEnvironment().findGitDir().build();

		if (newlyCreated)
		{
			final boolean hasRemote = !remote.equalsIgnoreCase("none");

			// Add the remote and pull from it
			if (hasRemote)
			{
				RepoHelper.addRemote(repo, "origin", remote);
				RepoHelper.pull(repo, "origin");
			}

			Git git = new Git(repo);

			// If there are no commits in this repository, create some
			if (!git.log().setMaxCount(1).call().iterator().hasNext())
			{
				git.commit().setAll(true).setAuthor("system", "system@localhost").setMessage("initial commit").call();
			}
		}

		return new ConfigRepository(repo);
	}
}
