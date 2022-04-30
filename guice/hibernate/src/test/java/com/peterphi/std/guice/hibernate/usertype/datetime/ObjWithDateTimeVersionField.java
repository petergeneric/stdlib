package com.peterphi.std.guice.hibernate.usertype.datetime;

import org.joda.time.DateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity(name = "test_entity")
class ObjWithDateTimeVersionField
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Version
	@Column(name = "updated_ts", nullable = false)
	private DateTime lastUpdated = DateTime.now();


	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public DateTime getLastUpdated()
	{
		return lastUpdated;
	}


	public void setLastUpdated(final DateTime lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
}
