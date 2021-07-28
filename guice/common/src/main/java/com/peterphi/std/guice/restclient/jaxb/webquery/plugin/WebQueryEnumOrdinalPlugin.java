package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.ConstraintContainer;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQFunctionType;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WebQueryEnumOrdinalPlugin implements WebQueryDecodePlugin
{
	private final String key;

	private final Map<String, Integer> ordinals = new HashMap<>();


	public <E extends Enum> WebQueryEnumOrdinalPlugin(final String key, final Class<? extends Enum> clazz)
	{
		this.key = key;

		for (Enum opt : clazz.getEnumConstants())
		{
			this.ordinals.put(opt.name().toLowerCase(Locale.ROOT), opt.ordinal());
		}
	}


	@Override
	public boolean handles(final String key)
	{
		return StringUtils.equalsIgnoreCase(this.key, key);
	}


	@Override
	public void process(final WebQuery query, final String key, final List<String> values)
	{
		final ConstraintContainer<?> container = (values.size() == 1) ? query : query.or();

		for (String arg : values)
		{
			final boolean isFunc = arg.startsWith("_");
			final WQFunctionType func = isFunc ? WQFunctionType.getByPrefix(arg) : WQFunctionType.EQ;

			switch (func)
			{
				case EQ:
					final String val = isFunc ? arg.substring(func.getPrefix().length()) : arg;
					container.eq(key, getOrdinal(val));
					break;
				case NEQ:
					container.neq(key, getOrdinal(arg.substring(func.getPrefix().length())));
					break;
				case IS_NULL:
					container.isNull(key);
					break;
				case NOT_NULL:
					container.isNotNull(key);
					break;
				default:
					throw new IllegalArgumentException("Unsupported function " + func + " for " + key);
			}
		}
	}


	/**
	 * Given a string enum name, return the ordinal value
	 *
	 * @param name the enum name (or an ordinal value)
	 * @return
	 */
	private int getOrdinal(final String name)
	{
		final Integer ord = ordinals.get(name.toLowerCase(Locale.ROOT));

		if (ord == null)
		{
			// Allow the user to supply an ordinal value directly
			try
			{
				if (Character.isDigit(name.charAt(0)))
					return Integer.valueOf(name);
			}
			catch (Throwable t)
			{
				// ignore parse error, return unsuported option error
			}


			throw new IllegalArgumentException("Unsupported option for option '" +
			                                   key +
			                                   "': " +
			                                   name +
			                                   ". Expected one of: " +
			                                   ordinals.keySet());
		}
		return ord;
	}
}
