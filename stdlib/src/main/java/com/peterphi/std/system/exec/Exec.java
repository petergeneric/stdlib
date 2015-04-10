package com.peterphi.std.system.exec;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An abstraction over ProcessBuilder to simplify the creation of processes
 */
public class Exec
{
	private static transient final Logger log = Logger.getLogger(Exec.class);

	/**
	 * Fake value we use to represent the superuser (instead of hard-coding "root" as the superuser)
	 */
	private final String SUPERUSER_IDENTIFIER = UUID.randomUUID().toString();

	private boolean spawned = false;

	private String runAs = null;
	protected final List<String> cmd = new ArrayList<String>();
	private final ProcessBuilder builder = new ProcessBuilder(cmd); // ProcessBuilder keeps a ref to the List (so we can modify it)
	private final Map<String, String> environment = new HashMap<String, String>();


	public Exec(String... command)
	{
		this(Arrays.asList(command));
	}


	public Exec(Iterable<String> command)
	{
		for (String segment : command)
		{
			this.cmd.add(segment);
		}
	}


	public File getWorkingDirectory()
	{
		File builderDirectory = builder.directory();

		if (builderDirectory == null)
		{
			String userDir = System.getProperty("user.dir");
			if (userDir != null)
				builderDirectory = new File(userDir);
		}

		return builderDirectory;
	}


	public Exec setWorkingDirectory(File dir)
	{
		builder.directory(dir);

		return this;
	}


	public Exec setEnv(String key, String value)
	{
		environment.put(key, value);

		return this;
	}


	public String getEnv(String key)
	{
		if (environment.containsKey(key))
			return environment.get(key);
		else
			return builder.environment().get(key);
	}


	public Exec runAsSuperuser()
	{
		return runAs(SUPERUSER_IDENTIFIER);
	}


	public Exec runAs(String username)
	{
		if (this.isSudoCommand())
			throw new IllegalStateException("Will not run a sudo command as another user!");

		this.runAs = username;

		return this;
	}


	public boolean getRedirectError()
	{
		return builder.redirectErrorStream();
	}


	public Exec setRedirectError(boolean value)
	{
		builder.redirectErrorStream(value);

		return this;
	}


	/**
	 * Launches the process, returning a handle to it for IO ops, etc<br />
	 * <strong>the caller must read the output streams: otherwise the buffers may fill up and the remote program will be
	 * suspended
	 * indefinitely</strong>
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public BasicProcessTracker startBasic() throws IOException
	{
		final Process p = getProcessBuilder().start();

		return new BasicProcessTracker(cmd, p, builder.redirectErrorStream());
	}


	/**
	 * Launches the process, returning a handle to it for IO ops, etc.<br />
	 * The finish condition for the OutputProcess is that all processes outputting to standard out must be complete before
	 * proceeding
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public Execed start() throws IOException
	{
		Process p = getProcessBuilder().start();

		return new Execed(cmd, p, builder.redirectErrorStream());
	}


	/**
	 * Returns a ProcessBuilder for use in a manual launching
	 *
	 * @return
	 */
	public ProcessBuilder getProcessBuilder()
	{
		if (spawned)
			return builder; // throw new IllegalStateException("Cannot call spawn twice!");

		if (runAs != null)
		{
			String command = cmd.get(0);

			if (command.charAt(0) == '-' && !SudoFeature.hasArgumentsEnd())
				throw new IllegalArgumentException("Command to runAs starts with - but this version of sudo does not support the -- argument end token: this command cannot, therefore, be executed securely. Command was: '" +
				                                   command + "'");

			// Pass the environment in through an "env" command:
			if (this.environment.size() > 0)
			{
				for (final String key : this.environment.keySet())
				{
					final String value = this.environment.get(key);

					final String var = key + "=" + value;

					addToCmd(0, var);
				}

				// cmd.add(0,"env");
				addToCmd(0, "env");
			}

			// cmd.add(0, "--"); // doesn't work everywhere
			// cmd.add(0, runAs);
			// cmd.add(0, "-u");
			// cmd.add(0, "-n"); // Never prompt for a password: we simply cannot provide one
			// cmd.add(0, "sudo");

			if (SudoFeature.hasArgumentsEnd())
				addToCmd(0, "--");

			// If possible tell sudo to run non-interactively
			String noninteractive = SudoFeature.hasNonInteractive() ? "-n" : null;

			if (this.runAs.equals(SUPERUSER_IDENTIFIER))
			{
				addToCmd(0, "sudo", noninteractive);
			}
			else
				addToCmd(0, "sudo", noninteractive, "-u", runAs);
		}
		else
		{
			builder.environment().putAll(this.environment);
		}

		spawned = true;

		if (log.isInfoEnabled())
		{
			log.info("ProcessBuilder created for command: " + join(" ", cmd));
		}

		return builder;
	}


	protected static String join(String with, String... strings)
	{
		return join(with, Arrays.asList(strings));
	}


	protected static String join(final String with, final List<String> strings)
	{
		final StringBuilder sb = new StringBuilder();

		boolean isFirst = true;
		for (final String str : strings)
		{
			if (!isFirst)
				sb.append(with);
			else
				isFirst = false;

			sb.append(str);
		}

		return sb.toString();
	}


	private void addToCmd(final int position, final String... args)
	{
		if (args != null)
			for (int i = args.length - 1; i != -1; i--)
			{
				if (args[i] != null)
				{
					cmd.add(position, args[i]);
				}
			}
	}


	/**
	 * Determines whether the command is trying to launch sudo
	 *
	 * @return
	 */
	boolean isSudoCommand()
	{
		return cmd.get(0).equals("sudo");
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and running as root
	 *
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */

	public static Execed rootUtility(String... command) throws IOException
	{
		return rootUtility(Arrays.asList(command));
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and running as root
	 *
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */

	public static Execed rootUtility(Iterable<String> command) throws IOException
	{
		Exec e = new Exec(command);
		e.runAsSuperuser();
		e.setRedirectError(true);

		return e.start();
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and optionally executing as a different user (eg root)
	 *
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */

	public static Execed utility(String... command) throws IOException
	{
		return utilityAs(null, command);
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and optionally executing as a different user (eg root)
	 *
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */

	public static Execed utility(Iterable<String> command) throws IOException
	{
		return utilityAs(null, command);
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and optionally executing as a different user (eg root)
	 *
	 * @param as
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */

	public static Execed utilityAs(String as, String... command) throws IOException
	{
		return utilityAs(as, Arrays.asList(command));
	}


	/**
	 * Runs a command in "utility" mode: redirecting stderr to stdout and optionally executing as a different user (eg root)
	 *
	 * @param as
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public static Execed utilityAs(String as, Iterable<String> command) throws IOException
	{
		Exec e = new Exec(command);
		if (as != null)
			e.runAs(as);
		e.setRedirectError(true);

		return e.start();
	}


	/**
	 * Runs a command, optionally executing as a different user (eg root)
	 *
	 * @param as
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public static Execed appAs(String as, String... command) throws IOException
	{
		return appAs(as, Arrays.asList(command));
	}


	/**
	 * Runs a command, optionally executing as a different user (eg root)
	 *
	 * @param as
	 * @param command
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public static Execed appAs(String as, Iterable<String> command) throws IOException
	{
		Exec e = new Exec(command);
		if (as != null)
			e.runAs(as);
		e.setRedirectError(false);

		return e.start();
	}
}
