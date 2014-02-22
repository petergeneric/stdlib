package com.peterphi.std.threading;

import org.apache.log4j.Logger;

/**
 * A special ParamRunnable which runs synchronously once prepare() is called
 *
 * @param <T>
 */
public abstract class ParamInvokeable<T> extends ParamRunnable<T>
{
	private static transient final Logger log = Logger.getLogger(ParamInvokeable.class);

	private boolean run = false;


	/**
	 * @see com.peterphi.std.threading.ParamRunnable#prepare(java.lang.Object)
	 * @deprecated if calling a ParamInvokeable directly the call(T) method should be used; this method is for maintaining full
	 * backwards compatibility with ParamRunnable
	 */
	@Deprecated
	@Override
	public final Runnable prepare(T param)
	{
		if (!prepared)
		{
			prepared = true;

			call(param);

			// Nothing to submit as a runnable
			return null;
		}
		else
		{
			throw new IllegalStateException("Cannot prepare() an already-prepared ParamRunnable");
		}
	}


	/**
	 * Synchronously executes this Invokeable
	 *
	 * @param param
	 */
	public final void call(T param)
	{
		if (!run)
		{
			run = true;
			prepared = true;

			try
			{
				this.run(param);
			}
			catch (Throwable t)
			{
				log.error("[ParamInvokeable] {prepare} : " + t.getMessage(), t);
			}
		}
	}
}
