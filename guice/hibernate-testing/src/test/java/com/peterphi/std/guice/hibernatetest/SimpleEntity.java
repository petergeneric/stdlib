package com.peterphi.std.guice.hibernatetest;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
class SimpleEntity
{
	@Id
	public long id;
	@Column
	public String name;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "simple_entity_join_table",
	           joinColumns = @JoinColumn(name = "simple_id", referencedColumnName = "id", nullable = false, updatable = false),
	           inverseJoinColumns = @JoinColumn(name = "group_id",
	                                            referencedColumnName = "id",
	                                            nullable = false,
	                                            updatable = false))
	private Set<GroupEntity> events = new HashSet<>();


	SimpleEntity()
	{
	}


	SimpleEntity(final long id, final String name, GroupEntity... groups)
	{
		this.id = id;
		this.name = name;

		this.events.addAll(Arrays.asList(groups));
	}
}
