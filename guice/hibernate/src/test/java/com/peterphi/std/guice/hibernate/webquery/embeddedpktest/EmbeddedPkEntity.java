package com.peterphi.std.guice.hibernate.webquery.embeddedpktest;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
class EmbeddedPkEntity
{
	private SomePrimaryKey id;

	private String name;


	@EmbeddedId
	public SomePrimaryKey getId()
	{
		return id;
	}


	public void setId(final SomePrimaryKey id)
	{
		this.id = id;
	}


	@Column(name = "some_name")
	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}
}
