package com.peterphi.std.guice.testwebapp.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="some_table")
public class SomeEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Column(name = "some_str")
	public String stringValue;


	public SomeEntity()
	{
	}


	public SomeEntity(final String stringValue)
	{
		this.stringValue = stringValue;
	}


	@Override
	public String toString()
	{
		return "SomeEntity{" +
		       "id=" + id +
		       ", stringValue='" + stringValue + '\'' +
		       '}';
	}
}
