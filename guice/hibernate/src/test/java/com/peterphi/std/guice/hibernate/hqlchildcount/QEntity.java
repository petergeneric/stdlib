package com.peterphi.std.guice.hibernate.hqlchildcount;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity(name = "Q")
class QEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private Integer capacity;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	private List<REntity> children;


	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public Integer getCapacity()
	{
		return capacity;
	}


	public void setCapacity(final Integer capacity)
	{
		this.capacity = capacity;
	}


	public List<REntity> getChildren()
	{
		return children;
	}


	public void setChildren(final List<REntity> children)
	{
		this.children = children;
	}
}
