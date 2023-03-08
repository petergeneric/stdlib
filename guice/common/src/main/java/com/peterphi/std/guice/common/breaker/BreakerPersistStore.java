package com.peterphi.std.guice.common.breaker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class BreakerPersistStore
{
	private static final Logger log = LoggerFactory.getLogger(BreakerPersistStore.class);

	@Inject(optional = true)
	@Named(GuiceProperties.BREAKERS_PERSIST_STORE)
	public String persistStore;

	@Inject(optional = true)
	@Named(GuiceProperties.CONTEXT_NAME)
	public String contextName;

	private String prefix;
	private boolean initialised = false;
	private boolean enabled = false;
	private File folder;
	private List<String> defaultTripped = null;


	public List<String> getDefaultTripped()
	{
		if (isEnabled())
		{
			if (defaultTripped == null)
			{
				final FilenameFilter filter = (dir, name) -> name.startsWith(prefix);

				final File[] files = folder.listFiles(filter);

				List<String> names;

				if (files == null || files.length == 0)
					names = Collections.emptyList();
				else
					names = new ArrayList<>();

				for (File file : files)
				{
					final String name = StringUtils.removeStart(file.getName(), prefix);

					names.add(name);
				}

				this.defaultTripped = names;

				return defaultTripped;
			}

			return defaultTripped;
		}
		else
		{
			return Collections.emptyList();
		}
	}


	public boolean isBreakerDefaultTripped(final String breakerName)
	{
		return getDefaultTripped().contains(breakerName);
	}


	public void setState(final String breakerName, final boolean isTripped)
	{
		if (isEnabled())
		{
			final File file = getFile(breakerName);

			try
			{
				if (isTripped && !file.exists())
					file.createNewFile();
				else if (!isTripped && file.exists())
					file.delete();
			}
			catch (Throwable t)
			{
				log.warn("Error persisting breaker value to {}: {}", file, t.getMessage(), t);
			}
		}
	}


	private File getFile(final String name)
	{
		return new File(folder, prefix + name);
	}


	private boolean isEnabled()
	{
		if (!initialised)
		{
			enabled = StringUtils.isNotEmpty(persistStore);

			if (enabled)
				folder = new File(persistStore);

			this.prefix = normalise(contextName) + ".";

			initialised = true;
		}

		return enabled;
	}


	private String normalise(final String input)
	{
		return StringUtils.replaceChars(input, "#/\\:%", "_____");
	}
}
