package com.peterphi.configuration.service.git;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RepoHelperTest
{
	@Test
	public void splitPath() throws Exception
	{
		final List<String> expected = Arrays.asList("", "path", "path/to", "path/to/folder");
		final List<String> actual = RepoHelper.splitPath("/path/to/folder");

		assertEquals(expected, actual);
	}
}
