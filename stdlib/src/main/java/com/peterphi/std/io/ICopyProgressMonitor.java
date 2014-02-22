package com.peterphi.std.io;

public interface ICopyProgressMonitor
{
	/**
	 * A method which MAY be called to inform the monitor of the total size
	 *
	 * @param bytes
	 */
	public void size(long bytes);


	public void start();


	/**
	 * A method which MAY be called to inform the monitor of the block size
	 *
	 * @param size
	 */
	public void blocksize(int size);


	public void progress(long bytes);


	public void failure();


	public void complete();
}
