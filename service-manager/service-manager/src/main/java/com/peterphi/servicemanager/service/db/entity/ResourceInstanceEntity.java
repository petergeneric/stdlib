package com.peterphi.servicemanager.service.db.entity;

import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Version;
import java.util.HashMap;
import java.util.Map;

@Entity(name = "resource_instance")
public class ResourceInstanceEntity
{
	private int id;
	private ResourceTemplateEntity template;

	private String templateRevision;
	private int templateRevisionCounter;

	private String provider;
	private String providerInstanceId;

	private ResourceInstanceState state;

	private Map<String, String> metadata = new HashMap<>();

	private DateTime created = new DateTime();
	private DateTime updated;


	@Id
	@GeneratedValue
	public int getId()
	{
		return id;
	}


	@JoinColumn(name = "template_id", nullable = false)
	@ManyToOne(optional = false)
	public ResourceTemplateEntity getTemplate()
	{
		return template;
	}


	@Column(name = "template_revision_commit", length = 255)
	public String getTemplateRevision()
	{
		return templateRevision;
	}


	@Column(name = "template_revision_counter")
	public int getTemplateRevisionCounter()
	{
		return templateRevisionCounter;
	}


	@Column(name = "provider_name", length = 255)
	public String getProvider()
	{
		return provider;
	}


	@Column(name = "provider_instance_id", length = 1024)
	public String getProviderInstanceId()
	{
		return providerInstanceId;
	}


	@Column(name = "resource_state")
	public ResourceInstanceState getState()
	{
		return state;
	}


	@ElementCollection
	@JoinTable(name = "resource_instance_metadata", joinColumns = @JoinColumn(name = "resource_instance_id"))
	@MapKeyColumn(name = "meta_name")
	@Column(name = "meta_val")
	public Map<String, String> getMetadata()
	{
		return metadata;
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


	public void setId(final int id)
	{
		this.id = id;
	}


	public void setTemplate(final ResourceTemplateEntity template)
	{
		this.template = template;
	}


	public void setTemplateRevision(final String templateRevision)
	{
		this.templateRevision = templateRevision;
	}


	public void setTemplateRevisionCounter(final int templateRevisionCounter)
	{
		this.templateRevisionCounter = templateRevisionCounter;
	}


	public void setProvider(final String provider)
	{
		this.provider = provider;
	}


	public void setProviderInstanceId(final String providerInstanceId)
	{
		this.providerInstanceId = providerInstanceId;
	}


	public void setState(final ResourceInstanceState state)
	{
		this.state = state;
	}


	public void setMetadata(final Map<String, String> metadata)
	{
		this.metadata = metadata;
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
