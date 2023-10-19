package com.peterphi.std.guice.metrics.health;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * A health determine if a given folder can be read/written
 */
public abstract class FolderAccessibleCheck extends AbstractHealthCheck
{


	@Override
	protected Result check() throws Exception
	{
		String path = getPath();

		{
			File f = new File(path);

			if (!f.canRead())
			{
				return Result.unhealthy("Cannot read from " + path);
			}
		}

		{

			File f = new File(path);

			if (doWriteCheck())
			{
				if (!f.canWrite())
				{
					return Result.unhealthy("Cannot write to " + path);
				}
			}

			if (doWriteTest())
			{

				UUID uuid = UUID.randomUUID();

				File testfile = new File(path, uuid.toString() + ".tmp");

				try
				{
					try
					{
						FileUtils.write(testfile, uuid.toString());

						String read = FileUtils.readFileToString(testfile);

						if (!read.equals(uuid.toString()))
						{
							return Result.unhealthy("Cannot read/write " + path);
						}
					}
					finally
					{
						testfile.delete();
					}
				}
				catch (IOException e)
				{
					return Result.unhealthy("Error reading/writing from " + path + " " + e.getMessage());
				}
			}
		}


		return Result.healthy();
	}


	/**
	 * The path to the folder concerned
	 * @return
	 */
	protected abstract String getPath();

	/**
	 * If true, a permissions based check check will be performed to see if this application can write to the folder
	 * @return
	 */
	protected abstract boolean doWriteCheck();

	/**
	 * If true, the ability to write to the folder will be check by actually performing a file write (the written file is then deleted)
	 * @return
	 */
	protected abstract boolean doWriteTest();

}
