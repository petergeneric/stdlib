package com.peterphi.rules.types;

import com.google.inject.Injector;
import com.peterphi.std.guice.common.stringparsing.TimeoutConverter;
import com.peterphi.std.threading.Timeout;
import org.joda.time.Duration;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by bmcleod on 14/09/2016.
 */
public class TimeoutObjectVar extends Variable
{
	@XmlAttribute(required = true, name = "value")
	String v;


	@Override
	public Object getValue(final Injector injector)
	{
		return new TimeoutConverter().convert(v);
	}
}
