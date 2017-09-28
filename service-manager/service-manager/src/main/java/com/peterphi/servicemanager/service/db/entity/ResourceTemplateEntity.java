package com.peterphi.servicemanager.service.db.entity;

import org.joda.time.DateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "resource_template")
public class ResourceTemplateEntity
{
	private String id;
	private String latestRevision;
	private int revisions;

	private List<ResourceInstanceEntity> instances = new ArrayList<>();

	private DateTime created = new DateTime();
	private DateTime updated;


	@Id
	@Column(name = "id", length = 255)
	public String getId()
	{
		return id;
	}


	@Column(name = "revision_commit", length = 255)
	public String getLatestRevision()
	{
		return latestRevision;
	}


	@Column(name = "revision_counter")
	public int getRevisions()
	{
		return revisions;
	}


	@OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("state")
	public List<ResourceInstanceEntity> getInstances()
	{
		return instances;
	}


	@Column(name = "created_ts")
	public DateTime getCreated()
	{
		return created;
	}


	@Column(name = "updated_ts")
	@Version
	public DateTime getUpdated()
	{
		return updated;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	public void setLatestRevision(final String latestRevision)
	{
		this.latestRevision = latestRevision;
	}


	public void setRevisions(final int revisions)
	{
		this.revisions = revisions;
	}


	public void setInstances(final List<ResourceInstanceEntity> instances)
	{
		this.instances = instances;
	}


	public void setCreated(final DateTime created)
	{
		this.created = created;
	}


	public void setUpdated(final DateTime updated)
	{
		this.updated = updated;
	}
}
