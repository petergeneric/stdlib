package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple WebQuery Decode Plugin that allows named presets for queries. Example usage:
 * <p><pre>new WebQueryPresetPlugin("preset")
 * 		.withAllowMultiple(false) // Allow multiple presets to be specified in a single query
 * 		.withFailIfUnsupported(true) // Throw an exception if an unsupported preset is used (the default)
 * 		.withOption("running", q -> q.eq("state", "QUEUED", "RUNNING", "PAUSED"))
 * 		.withOption("important", q -> q.ge("priority", "10"))
 * 		.withOption("terminated", q -> q.eq("state", "FAILED", "SUCCESS", "CANCELLED"))</pre></p>
 */
public class WebQueryPresetPlugin implements WebQueryDecodePlugin
{
	private final String key;
	private boolean failIfUnsupported = true;
	private boolean allowMultiple = false;

	private final Map<String, Consumer<WebQuery>> opts = new HashMap<>();
	

	public WebQueryPresetPlugin(final String keyName)
	{
		this.key = keyName;
	}


	public WebQueryPresetPlugin withFailIfUnsupported(final boolean value)
	{
		this.failIfUnsupported = value;
		return this;
	}


	public WebQueryPresetPlugin withAllowMultiple(final boolean value)
	{
		this.allowMultiple = value;
		return this;
	}


	public WebQueryPresetPlugin withOption(final String value, final Consumer<WebQuery> handler)
	{
		opts.put(value.toLowerCase(Locale.ROOT), handler);

		return this;
	}


	@Override
	public boolean handles(final String key)
	{
		return StringUtils.equalsIgnoreCase(this.key, key);
	}


	@Override
	public void process(final WebQuery query, final String key, final List<String> values)
	{
		if (!allowMultiple && values.size() > 1)
			throw new IllegalArgumentException("Multiple values supplied for single-value option: " + key);

		for (String value : values)
		{
			final Consumer<WebQuery> handler = opts.get(value.toLowerCase(Locale.ROOT));

			if (handler != null)
				handler.accept(query);
			else if (failIfUnsupported)
				throw new IllegalArgumentException("Unsupported option for option '" +
				                                   key +
				                                   "': " +
				                                   value +
				                                   ". Expected one of: " +
				                                   opts.keySet());
		}
	}
}
