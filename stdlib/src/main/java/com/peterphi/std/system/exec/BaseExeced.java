package com.peterphi.std.system.exec;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.io.*;
import com.peterphi.std.io.StreamUtil;
import com.peterphi.std.threading.Deadline;

public class BaseExeced {
	public final Object monitor = new Object();
	protected final List<String> cmd;
	protected final Process process;
	protected final boolean combinedOutput;

	protected boolean finished = false;
	protected int exitCode = Integer.MIN_VALUE;


	protected BaseExeced(final List<String> cmd, final Process p, final boolean combinedOutput) {
		this.cmd = cmd;
		this.process = p;
		this.combinedOutput = combinedOutput;
	}


	public void kill() {
		process.destroy();
	}


	/**
	 * Determines if the application has completed yet
	 * 
	 * @return true if the process has terminated, otherwise false
	 */
	public boolean isFinished() {
		if (finished)
			return true;

		try {
			final int code = exitCode();

			finished(code);

			return true;
		}
		catch (IllegalThreadStateException e) {
			return false;
		}
	}


	/**
	 * Returns the exit code of the application, assuming it has already terminated. If the process has not yet terminated then an IllegalStateException will be thrown
	 * 
	 * @return the exit code of the process
	 * @throws IllegalStateException if the process has not yet terminated
	 */
	public int exitCode() throws IllegalThreadStateException {
		if (finished)
			return exitCode;

		return process.exitValue();
	}


	/**
	 * Wait an indefinite amount of time for the process to exit, expecting the return code to be <code>expected</code>. If the output is not <code>expected</code> then a RuntimeException is thrown<br />
	 * 
	 * @param expected the expected return code
	 * @return the exit code of the process
	 * @throws RuntimeException if the return code was not what was expected
	 */
	public int waitForExit(final int expected) {
		return waitForExit(Deadline.MAX_VALUE, expected);
	}


	/**
	 * Wait until <code>deadline</code> for the process to exit, expecting the return code to be <code>expected</code>. If the output is not <code>expected</code> (or if the operation times out) then a RuntimeException is thrown<br />
	 * In the event of a timeout the process is not terminated
	 * 
	 * @param deadline
	 * @param expected
	 * @return the exit code of the process
	 * @throws RuntimeException if a timeout occurrs or if the return code was not what was expected
	 */
	public int waitForExit(final Deadline deadline, final int expected) {
		final int code = waitForExit(deadline);

		if (code == Integer.MIN_VALUE)
			throw new RuntimeException(
					"Unexpected timeout while waiting for exit and expecting code " + expected,
					new TimeoutException("waitForExit timed out"));
		else if (code != expected)
			throw new RuntimeException("Unexpected code: wanted " + expected + " but got " + code);
		else
			return code;
	}


	/**
	 * Wait an indefinite amount of time for the process to exit
	 * 
	 * @return the exit code of the process
	 */
	public int waitForExit() {
		return waitForExit(Deadline.MAX_VALUE);

	}


	/**
	 * Waits for the process to exit; this method blocks until the process has completed (at which point it returns the process exit code) or until <code>deadline</code> has elapsed, at which point it returns Integer.MIN_VALUE
	 * 
	 * @param deadline
	 * @return the exit code of the process
	 */
	public int waitForExit(Deadline deadline) {
		final int intervalMax = 4500; // the maximum time between polls
		int interval = 5; // initial sleep will be 3* this, so 15

		while (!isFinished() && deadline.isValid())
			synchronized (this.monitor) {
				try {
					// Check very frequently initially, tripling the wait each time
					// this allows us to return very quickly for short-running processes, but not hammer the CPU for long-running processes
					if (interval != intervalMax) {
						interval = Math.min(interval * 3, intervalMax);
					}
					this.monitor.wait(Math.min(deadline.getTimeLeft(), interval));
				}
				catch (InterruptedException e) {
				}
			}

		if (deadline.isExpired())
			return Integer.MIN_VALUE;
		else
			return exitCode();
	}


	public InputStream getStandardOutputStream() {
		return process.getInputStream();
	}


	public InputStream getStandardErrorStream() {
		return process.getErrorStream();
	}


	public OutputStream getStandardInputStream() {
		return process.getOutputStream();
	}


	/**
	 * Indicates that the output of this process should be discarded
	 */
	public void discardOutput() {
		discard(getStandardOutputStream());
		discard(getStandardErrorStream());
	}


	protected void finished(int exitCode) {
		synchronized (this.monitor) {
			this.finished = true;
			this.exitCode = exitCode;

			this.monitor.notifyAll();
		}
	}


	protected void unexpectedFailure(IOException e) {
		if (finished)
			return;

		if (!isFinished())
			finished(Integer.MIN_VALUE);
	}


	// Commence a background copy
	protected Thread copy(final InputStream in, final Writer out) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					StreamUtil.streamCopy(in, out);
				}
				catch (IOException e) {
					try {
						out.flush();
					}
					catch (Throwable t) {}

					unexpectedFailure(e);
				}
			}
		};

		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();

		return t;
	}


	protected Thread discard(final InputStream in) {
		if (in == null)
			return null;

		Runnable r = new Runnable() {
			@Override
			public void run() {
				StreamUtil.eatInputStream(in);
			}
		};

		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();

		return t;
	}

	public static BaseExeced spawn(Exec e) throws IOException {
		ProcessBuilder pb = e.getProcessBuilder();

		pb.start();

		return new BaseExeced(e.cmd, pb.start(), pb.redirectErrorStream());
	}

}
