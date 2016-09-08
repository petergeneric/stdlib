package com.peterphi.servicemanager.service.db.entity;

import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity(name = "service_instance")
public class ServiceInstanceEntity
{
	private String id;
	private String codeRevision;
	private String endpoint;
	private String managementToken;
	private DateTime created = DateTime.now();
	private DateTime updated = DateTime.now();


	@Id
	@Column(name = "id", length = 255)
	public String getId()
	{
		return id;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	@Column(name = "code_revision", length = 1024)
	public String getCodeRevision()
	{
		return codeRevision;
	}


	public void setCodeRevision(final String codeRevision)
	{
		this.codeRevision = codeRevision;
	}


	@Column(name = "service_href", length = 1024)
	public String getEndpoint()
	{
		return endpoint;
	}


	public void setEndpoint(final String endpoint)
	{
		this.endpoint = endpoint;
	}


	@Column(name = "management_token", length = 1024)
	public String getManagementToken()
	{
		return managementToken;
	}


	public void setManagementToken(final String managementToken)
	{
		this.managementToken = managementToken;
	}


	@Column(name = "created_ts", nullable = false)
	public DateTime getCreated()
	{
		return created;
	}


	public void setCreated(final DateTime created)
	{
		this.created = created;
	}


	@Version
	@Column(name = "updated_ts", nullable = false)
	public DateTime getUpdated()
	{
		return updated;
	}


	public void setUpdated(final DateTime updated)
	{
		this.updated = updated;
	}
}
