package com.peterphi.std.system.exec;

import java.util.List;

/**
 * A basic process tracker that does not touch stdout/stderr streams
 */
public class BasicProcessTracker extends AbstractProcessTracker
{
	protected BasicProcessTracker(final List<String> cmd, final Process p, final boolean combinedOutput)
	{
		super(cmd, p, combinedOutput);
	}


	@Override
	protected boolean isStillReadingOutput()
	{
		return false; // we do not read output
	}


	@Override
	public void discardOutput()
	{
		throw new RuntimeException("BasicExec does not permit discarding output!");
	}
}
