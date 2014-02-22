package com.peterphi.std.threading;

/**
 * The Runnable shell in which a ParamRunnable is wrapped once its <code>prepare()</code> method is called
 *
 * @param <T>
 */
public final class ParamRunnableShell<T> implements Runnable
{
	final ParamRunnable<T> obj;
	final T param;


	protected ParamRunnableShell(ParamRunnable<T> obj, T param)
	{
		this.obj = obj;
		this.param = param;
	}


	@Override
	public void run()
	{
		obj.run(param);
	}
}
