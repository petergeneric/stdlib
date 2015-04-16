package com.peterphi.std.guice.hibernate.webquery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
class MyObject
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "obj_name")
	private String name;

	@Column(name = "deprecated")
	private boolean deprecated = false;

	@Column(name = "someBytes")
	@Lob
	private byte[] someBytes = "some bytes".getBytes();

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "other_object_id", nullable = true)
	private MyOtherObject otherObject;


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


	boolean isDeprecated()
	{
		return deprecated;
	}


	void setDeprecated(final boolean deprecated)
	{
		this.deprecated = deprecated;
	}


	public byte[] getSomeBytes()
	{
		return someBytes;
	}


	public void setSomeBytes(final byte[] someBytes)
	{
		this.someBytes = someBytes;
	}


	public MyOtherObject getOtherObject()
	{
		return otherObject;
	}


	public void setOtherObject(final MyOtherObject otherObject)
	{
		this.otherObject = otherObject;
	}
}
