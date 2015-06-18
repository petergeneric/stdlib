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
class ParentEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private Integer capacity;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	private List<ChildEntity> children;


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


	public List<ChildEntity> getChildren()
	{
		return children;
	}


	public void setChildren(final List<ChildEntity> children)
	{
		this.children = children;
	}
}
