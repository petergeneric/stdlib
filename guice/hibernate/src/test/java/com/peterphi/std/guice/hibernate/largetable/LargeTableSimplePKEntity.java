package com.peterphi.std.guice.hibernate.largetable;

import com.peterphi.std.guice.database.annotation.LargeTable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@LargeTable
@Entity(name = "simple_pk_entity")
 class LargeTableSimplePKEntity
{
	@Id
	@GeneratedValue
	public Long id;

	@Column(nullable = false)
	public String name;


	public LargeTableSimplePKEntity()
	{
	}


	public LargeTableSimplePKEntity(final String name)
	{
		this.name = name;
	}


	@Override
	public String toString()
	{
		return "LargeTableSimplePKEntity{" +
		       "id=" + id +
		       ", name='" + name + '\'' +
		       '}';
	}
}

