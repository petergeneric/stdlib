package com.peterphi.std.threading;

/**
 * A class encapsulating the Runnable idea, mixed with Callable - instead of having a return type, this takes an argument to be
 * passed to the <code>run</code> method<br />
 *
 * @param T
 * 		the parameter type
 */
public abstract class ParamRunnable<T>
{
	protected boolean prepared = false;


	public Runnable prepare(T param)
	{
		if (!prepared)
		{
			prepared = true;
			return new ParamRunnableShell<T>(this, param);
		}
		else
		{
			throw new IllegalStateException("Cannot prepare() an already-prepared ParamRunnable");
		}
	}


	public abstract void run(T t);
}
