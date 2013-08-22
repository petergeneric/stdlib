package com.mediasmiths.std.config;

import java.util.*;
import com.mediasmiths.std.config.annotation.Optional;
import com.mediasmiths.std.types.*;

public class ArrayConfigFile {
	public static final int[] DEFAULT_OPTONAL_INT_ARRAY = new int[] { 1, 2, 3, 4, 5 };
	public int[] intArray;
	public String[] stringArray;
	public Id[] idArray;
	public List<String> stringList;

	@Optional
	public int[] optionalIntArray = DEFAULT_OPTONAL_INT_ARRAY;
}
