package com.peterphi.std.guice.common.breaker;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class TripRecord
{
	public final Date when;
	public final String note;
	public final boolean newValue;


	public TripRecord(final String note, final boolean newValue)
	{
		this(new Date(), note, newValue);
	}


	public TripRecord(final Date when, final String note, final boolean newValue)
	{
		this.when = when;
		this.note = StringUtils.trimToEmpty(note);
		this.newValue = newValue;
	}


	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("when", when).add("note", note).add("newValue", newValue).toString();
	}
}
