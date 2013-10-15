package com.mediasmiths.std.guice.hibernate.hqlchildcount;

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
	private Long id;
	private Integer capacity;
	private List<REntity> children;


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


	@Column(nullable = false)
	public Integer getCapacity()
	{
		return capacity;
	}


	public void setCapacity(final Integer capacity)
	{
		this.capacity = capacity;
	}


	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	public List<REntity> getChildren()
	{
		return children;
	}


	public void setChildren(final List<REntity> children)
	{
		this.children = children;
	}
}
