package com.peterphi.std.guice.hibernate.webquery;

import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "mapped_superclass_test_entity")
@BatchSize(size = 250)
public class MappedSuperclassEntity extends AbstractEntity<SomeStateEnum>
{
	private Long id;

	private String name;

	private SomeStateEnum state;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId()
	{
		return id;
	}


	@Column(name = "obj_name")
	public String getName()
	{
		return name;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	@Override
	public SomeStateEnum getState()
	{
		return this.state;
	}


	@Override
	public void setState(final SomeStateEnum state)
	{
		this.state = state;
	}
}
