package com.peterphi.std.guice.hibernate.hqlchildcount;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "R")
class REntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private QEntity parent;

	@Column(nullable = false)
	private boolean flag;


	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public QEntity getParent()
	{
		return parent;
	}


	public void setParent(final QEntity parent)
	{
		this.parent = parent;
	}


	public boolean isFlag()
	{
		return flag;
	}


	public void setFlag(final boolean flag)
	{
		this.flag = flag;
	}
}
