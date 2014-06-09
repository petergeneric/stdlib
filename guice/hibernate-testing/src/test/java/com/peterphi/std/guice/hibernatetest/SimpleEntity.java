package com.peterphi.std.guice.hibernatetest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
class SimpleEntity
{
	@Id
	public long id;
	@Column
	public String name;


	SimpleEntity()
	{
	}


	SimpleEntity(final long id, final String name)
	{
		this.id = id;
		this.name = name;
	}
}
