package com.mediasmiths.std.config.parser;

import com.mediasmiths.std.config.parser.impl.ArrayParser;
import com.mediasmiths.std.config.parser.impl.BooleanParser;
import com.mediasmiths.std.config.parser.impl.ByteParser;
import com.mediasmiths.std.config.parser.impl.CharacterParser;
import com.mediasmiths.std.config.parser.impl.ClassParser;
import com.mediasmiths.std.config.parser.impl.DoubleParser;
import com.mediasmiths.std.config.parser.impl.EnumParser;
import com.mediasmiths.std.config.parser.impl.ExtensibleEnumParser;
import com.mediasmiths.std.config.parser.impl.FileParser;
import com.mediasmiths.std.config.parser.impl.FloatParser;
import com.mediasmiths.std.config.parser.impl.GenericObjectParser;
import com.mediasmiths.std.config.parser.impl.IdParser;
import com.mediasmiths.std.config.parser.impl.IntParser;
import com.mediasmiths.std.config.parser.impl.IpParser;
import com.mediasmiths.std.config.parser.impl.LongParser;
import com.mediasmiths.std.config.parser.impl.MapParser;
import com.mediasmiths.std.config.parser.impl.ShortParser;
import com.mediasmiths.std.config.parser.impl.StringParser;
import com.mediasmiths.std.config.parser.impl.URLParser;
import com.mediasmiths.std.config.parser.impl.UUIDParser;
import com.mediasmiths.std.config.parser.impl.X500PrincipalParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Parser Factory: creates parsers for various types.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public final class ParserFactory {
	/**
	 * @return
	 */
	public static ParserFactory getInstance() {
		return new ParserFactory();
	}

	public static final List<IConfigParser> DEFAULT_PARSERS = new ArrayList<IConfigParser>();

	// Register the basic set of parsers
	static {
		// Add parsers for primitive types
		Collections.addAll(
				DEFAULT_PARSERS,
				new MapParser(),
				new ArrayParser(),
				new BooleanParser(),
				new ByteParser(),
				new CharacterParser(),
				new ClassParser(),
				new DoubleParser(),
				new FloatParser(),
				new IntParser(),
				new LongParser(),
				new ShortParser(),
				new StringParser());

		// Add parsers for standard Java types
		Collections.addAll(
				DEFAULT_PARSERS,
				new EnumParser(),
				new IpParser(),
				new FileParser(),
				new URLParser(),
				new UUIDParser(),
				new X500PrincipalParser());

		// Add parsers for com.mediasmiths.std types
		Collections.addAll(DEFAULT_PARSERS, new IdParser(), new ExtensibleEnumParser());

	}


	public final <T> IConfigParser<T> getProvider(final Class<T> t) {
		return getProvider(t, null);
	}


	public final <T> IConfigParser<T> getProvider(final Class<T> t, final Field field) {
		for (IConfigParser parser : DEFAULT_PARSERS) {
			if (parser.canParse(t)) {
				return parser;
			}
		}

		return GenericObjectParser.INSTANCE;
	}

}
