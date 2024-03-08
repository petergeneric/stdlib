package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Defines a plugin that extends the functionality of the WebQuery Query String Decode functionality
 */
public interface WebQueryDecodePlugin
{
	/**
	 * Called before the native WebQuery.decode handles the provided key. If this method returns true the plugin is responsible
	 * for decoding/processing the key.
	 *
	 * @param key the query string key
	 * @return true if this plugin should handle the named key
	 */
	default boolean handles(final String key, final List<String> values)
	{
		return handles(key);
	}

	/**
	 * Called before the native WebQuery.decode handles the provided key. If this method returns true the plugin is responsible
	 * for decoding/processing the key. Simple convenience method so implementors do not need to implement {@link #handles(String,
	 * List)}
	 *
	 * @param key the query string key
	 * @return true if this plugin should handle the named key
	 */
	boolean handles(final String key);

	/**
	 * Called to request that the plugin add the appropriate constraints to <code>query</code>
	 *
	 * @param query  the WebQuery being constructed
	 * @param key    the key (which this object has previously indicated that it {@link #handles(String)})
	 * @param values the values provided at the query string
	 */
	void process(final WebQuery query, final String key, final List<String> values);


	class Builder
	{
		private final List<WebQueryDecodePlugin> pre = new ArrayList<>();
		private final List<WebQueryDecodePlugin> mid = new ArrayList<>();


		public Builder()
		{
		}


		public Builder with(WebQueryDecodePlugin plugin)
		{
			mid.add(plugin);
			return this;
		}


		/**
		 * Add a validator to limit the values that may be provided by the user for a given key
		 *
		 * @param key       the key to validate
		 * @param predicate condition which must return true for all values for this key
		 * @return this builder instance for chaining
		 */
		public Builder validate(final String key, final Predicate<String> predicate)
		{
			pre.add(new ValidateKeyPlugin(key, predicate));
			return this;
		}


		/**
		 * Indicates that WebQuery Text queries should be banned
		 *
		 * @return this builder instance for chaining
		 */
		public Builder banTextQuery()
		{
			return ban("q");
		}


		/**
		 * Indicates that the provided set of keys may not be specified by the user. This can include control fields
		 *
		 * @param keys a list of keys to ban
		 * @return this builder instance for chaining
		 */
		public Builder ban(final String... keys)
		{
			pre.add(new DenyKeyPlugin(Arrays.asList(keys), true, false));
			return this;
		}


		/**
		 * Allow only the provided set of keys, and special keys. Generally it will also be necessary to ban text queries too
		 * using {@link #banTextQuery()}
		 *
		 * @param keys the keys to be permitted (in addition, special _ keys will be permitted)
		 * @return this builder instance for chaining
		 */
		public Builder allowOnly(final String... keys)
		{
			pre.add(new DenyKeyPlugin(Arrays.asList(keys), false, true));
			return this;
		}


		/**
		 * Allow only the provided set of keys. Any special keys must also be explicitly whitelisted
		 *
		 * @param keys the keys to be permitted
		 * @return this builder instance for chaining
		 */
		public Builder allowOnlyStrict(final String... keys)
		{
			pre.add(new DenyKeyPlugin(Arrays.asList(keys), false, false));
			return this;
		}


		private List<WebQueryDecodePlugin> getPlugins()
		{
			List<WebQueryDecodePlugin> list = new ArrayList<>();

			list.addAll(pre);
			list.addAll(mid);

			list.removeIf(Objects :: isNull);

			return list;
		}


		public WebQueryDecodePlugin build()
		{
			final List<WebQueryDecodePlugin> plugins = getPlugins();

			if (plugins.isEmpty())
				return null;
			else if (plugins.size() == 1)
				return plugins.get(0);
			else
				return new UnionPlugin(plugins.toArray(new WebQueryDecodePlugin[0]));
		}
	}
}
