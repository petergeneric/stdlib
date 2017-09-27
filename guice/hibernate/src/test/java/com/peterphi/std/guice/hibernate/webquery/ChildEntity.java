package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.database.annotation.EagerFetch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
class ChildEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "obj_name")
	private String name;

	@EagerFetch
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private ParentEntity parent;


	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	public ParentEntity getParent()
	{
		return parent;
	}


	public void setParent(final ParentEntity parent)
	{
		this.parent = parent;
	}
}
