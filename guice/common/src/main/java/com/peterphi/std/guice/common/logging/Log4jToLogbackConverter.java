package com.peterphi.std.guice.common.logging;

import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;

/**
 * Converts a log4j.properties of a very simple and fixed structure to a simple logback console appender config, e.g.
 * <pre>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;configuration&gt;
 *
 *   &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt;
 *     &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *       &lt;Pattern&gt;%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n&lt;/Pattern&gt;
 *     &lt;/layout&gt;
 *   &lt;/appender&gt;
 *
 *   &lt;logger name="com.base22" level="TRACE"/&gt;
 *
 *
 *   &lt;root level="debug"&gt;
 *     &lt;appender-ref ref="STDOUT" /&gt;
 *   &lt;/root&gt;
 * &lt;/configuration&gt;</pre>
 */
final class Log4jToLogbackConverter
{
	public static String convert(PropertyFile properties)
	{
		final String LOGGER_PREFIX = "log4j.logger.";

		StringBuilder sb = new StringBuilder();

		final String rootCategory = properties.get("log4j.rootCategory");

		final String rootLevel;
		final String defaultAppender;
		if (rootCategory != null && rootCategory.contains(","))
		{
			rootLevel = StringUtils.trimToNull(StringUtils.split(rootCategory, ',')[0]);
			defaultAppender = StringUtils.trimToNull(StringUtils.split(rootCategory, ',')[1]);
		}
		else
		{
			// Apply sensible defaults
			rootLevel = "WARN";
			defaultAppender = "logconsole";
		}

		final String pattern = properties.get("log4j.appender." + defaultAppender + ".layout.ConversionPattern",
		                                      LoggingModule.DEFAULT_OUTPUT_PATTERN);

		sb.append(
				"<configuration><appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\"><encoder class=\"ch.qos.logback.classic.encoder.PatternLayoutEncoder\"><Pattern>");
		sb.append(pattern);
		sb.append("</Pattern></encoder></appender>");

		for (String key : properties.keySet())
		{
			if (key.startsWith(LOGGER_PREFIX))
			{
				final String path = StringUtils.replaceOnce(key, LOGGER_PREFIX, "");
				final String level = properties.get(key);

				sb.append("<logger name=\"" + path + "\" level=\"" + level + "\"/>");
			}
		}

		sb.append("<root level=\"" + rootLevel + "\"><appender-ref ref=\"STDOUT\" /></root></configuration>");

		return sb.toString();
	}
}
