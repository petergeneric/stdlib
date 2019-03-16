package com.peterphi.usermanager.daemon;

import com.google.inject.Inject;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.threading.Timeout;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

@EagerSingleton
@Doc("Runs background maintenance tasks on the database")
public class BackgroundTaskDaemon extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(BackgroundTaskDaemon.class);

	@Inject
	UserAuthenticationService authenticationService;


	public BackgroundTaskDaemon()
	{
		super(new Timeout(1, TimeUnit.HOURS));
	}


	@Override
	protected void execute() throws Exception
	{
		try
		{
			setTextState("Running background tasks...");
			authenticationService.executeBackgroundTasks();
		}
		catch (Throwable t)
		{
			log.error("User Manager Background Task error", t);
			setTextState("Failed: " + t.getMessage());
		}
	}
}
