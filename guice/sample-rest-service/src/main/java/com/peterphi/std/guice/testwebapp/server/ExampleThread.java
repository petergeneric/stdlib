package com.peterphi.std.guice.testwebapp.server;

import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

@EagerSingleton
public class ExampleThread extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(ExampleThread.class);


	public ExampleThread()
	{
		super(new Timeout(5, TimeUnit.HOURS));
	}


	@Override
	protected void execute() throws Exception
	{
		log.info("ExampleThread would do some work now");
	}
}
