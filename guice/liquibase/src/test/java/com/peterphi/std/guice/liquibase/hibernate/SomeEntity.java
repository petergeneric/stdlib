package com.peterphi.std.guice.liquibase.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "something")
public class SomeEntity
{
	@Id
	public long id;

	@Column
	public String name;
}
