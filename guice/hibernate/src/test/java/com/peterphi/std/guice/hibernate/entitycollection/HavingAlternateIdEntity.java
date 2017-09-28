package com.peterphi.std.guice.hibernate.entitycollection;

import com.google.common.base.Objects;
import com.peterphi.std.guice.database.annotation.EagerFetch;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "having_altid")
public class HavingAlternateIdEntity
{
	private Long id;
	private Set<AlternateIdEmbeddedEntity> alternateIds = new HashSet<>();


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
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "asset_alternate_id", joinColumns = @JoinColumn(name = "asset_id"))
	public Set<AlternateIdEmbeddedEntity> getAlternateIds()
	{
		return alternateIds;
	}


	public void setAlternateIds(final Set<AlternateIdEmbeddedEntity> alternateIds)
	{
		this.alternateIds = alternateIds;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("id", id).add("alternateIds", alternateIds).toString();
	}
}
