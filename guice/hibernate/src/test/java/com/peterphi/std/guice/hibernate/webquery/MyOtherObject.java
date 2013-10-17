package com.peterphi.std.guice.hibernate.webquery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
class MyOtherObject
{
	private Long id;
	private String name;
	private MyObject parent;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	@Column(name = "obj_name")
	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id", nullable = true)
	public MyObject getParent()
	{
		return parent;
	}


	public void setParent(final MyObject parent)
	{
		this.parent = parent;
	}
}
