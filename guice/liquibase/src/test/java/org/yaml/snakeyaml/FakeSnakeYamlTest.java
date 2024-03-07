package org.yaml.snakeyaml;

import liquibase.parser.core.yaml.YamlChangeLogParser;
import liquibase.parser.core.yaml.YamlSnapshotParser;
import org.junit.Test;

/**
 * We ship fake snakeyaml classes that implement ONLY the methods used by liquibase's <code>YamlChangeLogParser</code> to load (since we do not use or permit YAML in our projects)<br />
 * This test exists to track whether a liquibase update is using new snakeyaml methods.<br />
 * This is all to avoid having snakeyaml as a dependency (since it would be 330k of dead weight)
 */
public class FakeSnakeYamlTest
{
	@Test
	public void testYamlChangeLogParser()
	{
		new YamlChangeLogParser();
	}


	@Test
	public void testUtils()
	{
		// This method would log an error if it fails, but want to make sure the class loads OK
		liquibase.util.SnakeYamlUtil.setCodePointLimitSafely(new LoaderOptions(), 1);
	}


	@Test
	public void testYamlSnapshotParser()
	{
		new YamlSnapshotParser();
	}
}
