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
	private Long id;
	private QEntity parent;
	private boolean flag;


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


	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public QEntity getParent()
	{
		return parent;
	}


	public void setParent(final QEntity parent)
	{
		this.parent = parent;
	}


	@Column(nullable = false)
	public boolean isFlag()
	{
		return flag;
	}


	public void setFlag(final boolean flag)
	{
		this.flag = flag;
	}
}
