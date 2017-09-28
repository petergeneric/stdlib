package com.peterphi.std.guice.hibernate.entitycollection;

import com.peterphi.std.guice.database.annotation.EagerFetch;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "refs_having_altid")
public class AlternateIdReferencingEntity
{
	private Long id;
	private HavingAlternateIdEntity referenced;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	@EagerFetch
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referenced_id")
	public HavingAlternateIdEntity getReferenced()
	{
		return referenced;
	}


	public void setReferenced(final HavingAlternateIdEntity referenced)
	{
		this.referenced = referenced;
	}
}
