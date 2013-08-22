package com.mediasmiths.std.types;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class SpeedyStringTest {
	@Test
	public void testAllocate() {
		String str = "abc";
		assertEquals(str, SpeedyString.fastAllocate(str.toCharArray()));
	}


	@Test
	public void testAllocateWithEmptyArray() {
		String str = "";
		assertEquals(str, SpeedyString.fastAllocate(str.toCharArray()));
	}


	@Test
	public void testChop() {
		String str = "abc";
		assertEquals("ab", SpeedyString.chop(str));
	}


	@Test
	public void testChopEmpty() {
		String str = "";
		assertEquals(str, SpeedyString.chop(str));
	}


	@Test
	public void testJoin() {
		assertEquals("a,b,c", SpeedyString.fastJoin(",", "a", "b", "c"));
	}


	@Test
	public void testJoinCharLists() {
		List<char[]> list = new ArrayList<char[]>();
		list.add("a".toCharArray());
		list.add("b".toCharArray());
		list.add("c".toCharArray());

		assertEquals("a,b,c", SpeedyString.fastJoin_chars(",", 5, list));
	}


	@Test
	public void testJoinStringArray() {
		assertEquals("a,b,c", SpeedyString.fastJoin_strings(",", 5, Arrays.asList("a", "b", "c")));
	}


	@Test
	public void testConcatStringArray() {
		assertEquals("abc", SpeedyString.fastConcat("a", "b", "c"));
	}


	@Test
	public void testConcatCharArrayArray() {
		assertEquals(
				"axbxcx",
				SpeedyString.fastConcat(new char[][] { "ax".toCharArray(), "bx".toCharArray(), "cx".toCharArray() }));
	}

}
