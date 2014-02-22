package com.peterphi.std.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ListUtilityTest
{
	@Test
	public void testHeadWithEmptyList()
	{
		List<String> list = Arrays.asList();

		assertNull(ListUtility.head(list));
	}


	@Test(expected = NullPointerException.class)
	public void testNPEWhenHeadWithNullList()
	{
		ListUtility.head(null);
	}


	@Test
	public void testHeadWithList()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertNotNull(ListUtility.head(list));
		assertEquals("a", ListUtility.head(list));
	}


	@Test
	public void testTailWithList()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertNotNull(ListUtility.tail(list));
		assertEquals(Arrays.asList("b", "c"), ListUtility.tail(list));
	}


	@Test
	public void testTailWithOneItemList()
	{
		List<String> list = Arrays.asList("a");

		assertNotNull(ListUtility.tail(list));
		assertEquals(Collections.EMPTY_LIST, ListUtility.tail(list));
	}


	@Test
	public void testTailWithEmptyList()
	{
		List<String> list = Arrays.asList();

		assertNotNull(ListUtility.tail(list));
		assertEquals(list, ListUtility.tail(list));
	}


	@Test
	public void testFlipWithEmptyList()
	{
		List<String> list = Arrays.asList();

		List<String> flipped = ListUtility.flip(list);
		assertNotNull(flipped);
	}


	@Test
	public void testFlipWithOneItemListIsSame()
	{
		List<String> list = Arrays.asList("a");

		List<String> flipped = ListUtility.flip(list);
		assertEquals(list, flipped);
	}


	@Test
	public void testFlipWithListReverses()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		List<String> flipped = ListUtility.flip(list);
		assertEquals(Arrays.asList("c", "b", "a"), flipped);
	}


	@Test
	public void testDoubleFlipIsIdentity()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertEquals(list, ListUtility.flip(ListUtility.flip(list)));
	}


	@Test
	public void testListAppliedToListIsCopyWithSameContent()
	{
		List<String> list = Arrays.asList("a", "b");

		assertTrue(list != ListUtility.list(list));
		assertEquals(list, ListUtility.list(list));
	}


	@Test
	public void testListAppliedToIterableIsCorrect()
	{
		List<String> list = Arrays.asList("a", "b");

		assertEquals(list, ListUtility.list((Iterable<String>) list));
	}


	@Test
	public void testLast()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertEquals(Arrays.asList("b", "c"), ListUtility.last(list, 2));
	}


	@Test
	public void testLastWithZeroIsEmpty()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertEquals(Arrays.asList(), ListUtility.last(list, 0));
	}


	@Test
	public void testLastWithGreaterThanCount()
	{
		List<String> list = Arrays.asList("a", "b", "c");

		assertEquals(list, ListUtility.last(list, 10));
	}
}
