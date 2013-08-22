package com.mediasmiths.std.system.exec;

import java.util.*;
import java.io.*;

public class Execed extends BaseExeced {
	// Outputs
	final StringWriter stdout_content = new StringWriter();
	final StringWriter stderr_content;

	final Thread stdoutRead;
	final Thread stderrRead;


	public static Execed spawn(Exec e) throws IOException {
		ProcessBuilder pb = e.getProcessBuilder();

		return new Execed(e.cmd, pb.start(), pb.redirectErrorStream());
	}


	protected Execed(List<String> cmd, Process p, boolean combinedOutput) {
		super(cmd, p, combinedOutput);

		if (combinedOutput) {
			stderr_content = null;

			stdoutRead = copy(p.getInputStream(), stdout_content);
			stderrRead = null;
		}
		else {
			stderr_content = new StringWriter();

			stdoutRead = copy(p.getInputStream(), stdout_content);
			stderrRead = copy(p.getErrorStream(), stderr_content);
		}

	}


	public String getStandardOut() {
		this.stdout_content.flush();

		return this.stdout_content.getBuffer().toString();
	}


	public String getStandardError() {
		if (stderr_content == null)
			return null;
		else
			return this.stderr_content.getBuffer().toString();
	}


	@Override
	public boolean isFinished() {
		return super.isFinished() && !stdoutRead.isAlive();
	}

	@Override
	public void discardOutput() {
		throw new RuntimeException("SimpleOutputGrabber cannot discard output!");
	}

	@Override
	public void kill() {
		process.destroy();
	}

}
