package com.peterphi.std.guice.hibernate.webquery;


import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
abstract class AbstractEntity
{
	private DateTime created = DateTime.now();
	private DateTime updated;


	@Column(name = "created_ts", nullable = false)
	public DateTime getCreated()
	{
		return created;
	}


	public void setCreated(final DateTime created)
	{
		this.created = created;
	}


	@Version
	@Column(name = "updated_ts", nullable = false)
	public DateTime getUpdated()
	{
		return updated;
	}


	public void setUpdated(final DateTime updated)
	{
		this.updated = updated;
	}
}
