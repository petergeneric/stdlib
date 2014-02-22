package com.peterphi.std.io;

import java.util.EnumSet;
import java.util.Set;

public enum ChmodBit
{
	OWNER_READ("u", "r"),
	OWNER_WRITE("u", "w"),
	OWNER_EXEC("u", "x"),
	GROUP_READ("g", "r"),
	GROUP_WRITE("g", "w"),
	GROUP_EXEC("g", "x"),
	OTHER_READ("o", "r"),
	OTHER_WRITE("o", "w"),
	OTHER_EXEC("o", "x");

	private final String group;
	private final String symbol;


	private ChmodBit(String group, String symbol)
	{
		this.group = group;
		this.symbol = symbol;
	}


	public static String toString(boolean add, Set<ChmodBit> bits)
	{
		if (bits == null || bits.size() == 0)
			return "";

		StringBuilder sb = new StringBuilder(bits.size() * 4); // technically we only need bits.size*3 + bits.size-1

		boolean first = true;
		for (ChmodBit bit : bits)
		{
			if (first)
				first = false;
			else
				sb.append(",");

			sb.append(bit.group);
			sb.append(add ? "+" : "-");
			sb.append(bit.symbol);
		}

		return sb.toString();
	}


	public static String toString(Set<ChmodBit> set, Set<ChmodBit> clear)
	{
		return join(",", toString(true, set), toString(false, clear));
	}


	public static EnumSet<ChmodBit> set(ChmodBit... bits)
	{
		EnumSet<ChmodBit> set = EnumSet.noneOf(ChmodBit.class);

		for (ChmodBit bit : bits)
			set.add(bit);

		return set;
	}


	private static String join(String joiner, String... entries)
	{
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (String entry : entries)
		{
			if (entry != null && entry.length() != 0)
			{
				if (first)
					first = false;
				else
					sb.append(joiner);

				sb.append(entry);
			}
		}

		return sb.toString();
	}
}
