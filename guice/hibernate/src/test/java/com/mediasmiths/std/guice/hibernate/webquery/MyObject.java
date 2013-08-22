package com.mediasmiths.std.guice.hibernate.webquery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Collection;

@Entity
class MyObject
{
	private Long id;
	private String name;
	private MyOtherObject otherObject;
	private Collection<MyObject> children;


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
	@JoinColumn(name = "other_object_id", nullable = true)
	public MyOtherObject getOtherObject()
	{
		return otherObject;
	}


	public void setOtherObject(final MyOtherObject otherObject)
	{
		this.otherObject = otherObject;
	}

}
