package com.mediasmiths.std.io;

import java.io.File;

public interface IFileChangeListener {
	public void fileChanged(File f, long prevModified, long lastModified);
}
