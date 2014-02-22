package com.peterphi.std.system.exec;

import com.peterphi.std.threading.Deadline;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Determines the features available from the locally installed sudo<br />
 * There are 2 key features: support for "--" and support for "-n"<br />
 * <em>-n</em> is the most useful (since if there's a privileges issue then control will return immediately)<br />
 * <em>--</em> increases security (since
 */
class SudoFeature
{
	private static transient final Logger log = Logger.getLogger(SudoFeature.class);

	// Some old versions of sudo don't support "--" to tell sudo that the user's command starts now
	private static boolean dashDashTested = false;
	private static boolean dashDashSupported;

	// Some versions of sudo don't support "-n" to tell sudo it's running non-interactively
	private static boolean dashNTested = false;
	private static boolean dashNSupported;

	// Some versions of sudo don't support "-n" to tell sudo it's running non-interactively
	private static boolean sudoTested = false;
	private static boolean sudoSupported;


	/**
	 * Determines whether a sane version of sudo is available which supports -n and -- (and, especially, does not claim to support
	 * -- and not, in fact, support it properly)
	 *
	 * @return
	 */
	public static boolean hasSaneSudo()
	{
		return SudoFeature.hasSudo() && SudoFeature.hasNonInteractive() && SudoFeature.hasArgumentsEnd();
	}


	/**
	 * Determines whether sudo is supported on the local machine
	 *
	 * @return
	 */
	public synchronized static boolean hasSudo()
	{
		if (!sudoTested)
		{
			String[] cmdArr = new String[]{"which", "sudo"};
			List<String> cmd = new ArrayList<String>(cmdArr.length);
			Collections.addAll(cmd, cmdArr);

			ProcessBuilder whichsudo = new ProcessBuilder(cmd);

			try
			{
				Process p = whichsudo.start();
				try
				{
					Execed e = new Execed(cmd, p, false);

					// Let it run
					int returnCode = e.waitForExit(new Deadline(3, TimeUnit.SECONDS));

					sudoSupported = (returnCode == 0);
					sudoTested = true;
				}
				finally
				{
					p.destroy();
				}
			}
			catch (Throwable t)
			{
				sudoSupported = false;
				sudoTested = true;
			}
		}

		return sudoSupported;
	}


	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	public synchronized static boolean hasNonInteractive()
	{
		if (!hasSudo())
			return false;

		if (!dashNTested)
		{
			// Try to run sudo -n whoami
			// There are 3 possible outputs:
			// 1. -n simply unsupported (returns 1, outputs "sudo: illegal option `-n'" on stderr)
			// 2. -n ignored (execution times out since sudo is waiting for a password) - unlikely to ever happen
			// 3. -n supported, user
			Process p = null;
			try
			{
				String[] cmdArr = new String[]{"sudo", "-n", "whoami"};
				List<String> cmd = new ArrayList<String>(cmdArr.length);
				Collections.addAll(cmd, cmdArr);

				ProcessBuilder whoami = new ProcessBuilder(cmd);

				p = whoami.start();
				Execed e = new Execed(cmd, p, false);

				// Let it run
				final int returnCode = e.waitForExit(Timeout.TEN_SECONDS.start());

				// code 0 (fully supported, operation worked)
				if (returnCode == 0)
				{
					dashNTested = true;
					dashNSupported = true;
				}
				else if (returnCode == 1)
				{
					// Take the simple approach and say it's unsupported
					dashNSupported = false;
					dashNTested = true;

					if (false)
					{
						String stderr = e.getStandardError();

						if (stderr.contains("-n"))
						{
							// Unsupported - outputs something like this on stderr: sudo: illegal option `-n'

							dashNSupported = false;
							dashNTested = true;
						}
						else
						{
							// Supported, password required. Output is something like: sudo: sorry, a password is required to run sudo
							dashNSupported = true;
							dashNTested = true;
						}
					}
				}
				else
				{
					if (returnCode == Integer.MIN_VALUE)
					{
						// Execution took longer than 3 seconds; -n was ignored
						dashNSupported = false;
						dashNTested = true;
					}
					else
					{
						throw new Error("sudo -n whoami returned " + returnCode + " (expected 0, 1 or Integer.MIN_VALUE)");
					}
				}
			}
			catch (Throwable t)
			{
				log.warn("{runasSupportsNoninteractiveMode} Cannot determine support. Exception: " + t.getMessage(), t);

				dashNSupported = false;
				dashNTested = true;
			}
			finally
			{
				// Make sure the process is terminated in case it's still running for whatever reason
				if (p != null)
					p.destroy();
			}
		}

		return dashNSupported;
	}


	/**
	 * Determines whether sudo on this machine supports the "--" option to indicate the end of the parseable arguments and the
	 * start of the user's command<br />
	 * This feature is currently dependent upon the "-n" feature (as well as the ability of the current user to whois as root with
	 * no password); this is not necessarily a bad thing, since old versions of sudo didn't seem to support -- properly
	 *
	 * @return
	 */
	public synchronized static boolean hasArgumentsEnd()
	{
		// If we can't execute "sudo -n" then don't even try to test this
		if (!hasSudo() || !hasNonInteractive())
			return false;

		if (!dashDashTested)
		{
			// Try running sudo -n whoami
			// If it works, try running sudo -n -- whoami
			// The result should be the same

			try
			{
				{
					Process p = null;
					try
					{
						String[] cmdArr = new String[]{"sudo", "-n", "whoami"};
						List<String> cmd = new ArrayList<String>(cmdArr.length);
						Collections.addAll(cmd, cmdArr);

						ProcessBuilder whoami = new ProcessBuilder(cmd);
						p = whoami.start();
						Execed e = new Execed(cmd, p, true);

						// Let it run
						e.waitForExit(0);
					}
					finally
					{
						if (p != null)
							p.destroy();
					}
				}

				{
					Process p = null;
					try
					{
						String[] cmdArr = new String[]{"sudo", "-n", "--", "whoami"};
						List<String> cmd = new ArrayList<String>(cmdArr.length);
						Collections.addAll(cmd, cmdArr);

						ProcessBuilder whoami = new ProcessBuilder(cmd);
						p = whoami.start();
						Execed e = new Execed(cmd, p, true);

						// Let it run
						final int returnCode = e.waitForExit();

						dashDashSupported = (returnCode == 0);
						dashDashTested = true;
					}
					finally
					{
						if (p != null)
							p.destroy();
					}
				}

			}
			catch (Throwable e)
			{
				dashDashSupported = false;
				dashDashTested = true;

				log.warn("{runasSupportsArgumentEndToken} Cannot determine support. Error: " + e.getMessage(), e);
			}
		}

		return dashDashSupported;
	}
}
