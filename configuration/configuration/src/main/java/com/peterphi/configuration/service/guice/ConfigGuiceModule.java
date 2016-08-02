package com.peterphi.configuration.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigRepository;
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
	public ConfigRepository getRepository(@Named("repo.config.path")
	                                      final File workingDirectory) throws IOException, GitAPIException
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
			Git git = new Git(repo);
			git.commit().setAll(true).setAuthor("system", "system@localhost").setMessage("initial commit").call();
		}

		return new ConfigRepository(repo);
	}
}
