package com.mediasmiths.std.io;

import java.util.*;
import java.io.*;
import com.mediasmiths.std.util.*;

public enum MagicBytes {
	ZIP, TAR, GZ, UNKNOWN;

	private static final Map<String, MagicBytes> registry = ListUtility.newMap();
	static {
		registry.put("1f8b", GZ); // a .gz stream
		// NB. testing for a TAR file isn't as easy & needs to be special-cased
		registry.put("504b0304", ZIP);
	}


	public static MagicBytes getMagic(File f) {
		String magic = getMagicBytes(f);

		for (String test : registry.keySet()) {
			if (magic.startsWith(test)) {
				return registry.get(test);
			}
		}

		return MagicBytes.UNKNOWN;
	}

	public static String getMagicBytes(File f) {
		if (!f.exists())
			throw new Error("Cannot get magic bytes for file that does not exist");

		byte[] buffer = new byte[4];

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		}
		catch (IOException e) {
			throw new Error(e.getMessage(), e);
		}
		finally {
			try {
				if (fis != null)
					fis.close();
			}
			catch (IOException e) {
				// ignore
			}
		}

		return HexHelper.toHex(buffer);
	}
}
