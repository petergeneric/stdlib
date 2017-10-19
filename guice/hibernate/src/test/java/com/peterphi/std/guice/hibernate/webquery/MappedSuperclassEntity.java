package com.peterphi.std.guice.hibernate.webquery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "mapped_superclass_test_entity")
public class MappedSuperclassEntity extends AbstractEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "obj_name")
	private String name;


	public Long getId()
	{
		return id;
	}


	public String getName()
	{
		return name;
	}
}
