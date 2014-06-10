package com.peterphi.std.guice.hibernatetest;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class GroupEntity
{
	@Id
	public long id;


	public GroupEntity(final long id)
	{
		this.id = id;
	}
}
