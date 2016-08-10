package com.peterphi.usermanager.db.entity;

import com.peterphi.std.types.SimpleId;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Arrays;
import java.util.List;

@Entity(name = "oauth_service")
public class OAuthServiceEntity
{
	private String id = SimpleId.alphanumeric("svc-", 36);
	private UserEntity owner;
	private String name;
	private String endpoints;
	private String clientSecret = SimpleId.alphanumeric("csec-", 36);
	private boolean enabled = true;
	private DateTime created = new DateTime();
	private DateTime updated = new DateTime();


	@Id
	@Column(name = "id", length = 36)
	public String getId()
	{
		return id;
	}


	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	public UserEntity getOwner()
	{
		return owner;
	}


	@Column(name = "client_name", length = 1000, nullable = false)
	public String getName()
	{
		return name;
	}


	@Column(name = "client_endpoints", length = 2000, nullable = false)
	public String getEndpoints()
	{
		return endpoints;
	}


	@Column(name = "client_secret", length = 36, nullable = false)
	public String getClientSecret()
	{
		return clientSecret;
	}


	@Column(name = "is_enabled", nullable = false)
	public boolean isEnabled()
	{
		return enabled;
	}


	@Column(name = "created_ts", nullable = false)
	public DateTime getCreated()
	{
		return created;
	}


	@Version
	@Column(name = "updated_ts", nullable = false)
	public DateTime getUpdated()
	{
		return updated;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	public void setOwner(final UserEntity owner)
	{
		this.owner = owner;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	public void setEndpoints(final String endpoints)
	{
		this.endpoints = endpoints;
	}


	public void setClientSecret(final String clientSecret)
	{
		this.clientSecret = clientSecret;
	}


	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;
	}


	public void setCreated(final DateTime created)
	{
		this.created = created;
	}


	public void setUpdated(final DateTime updated)
	{
		this.updated = updated;
	}

	//
	// Helpers
	//


	public List<String> endpointsAsList()
	{
		return Arrays.asList(getEndpoints().split("\n"));
	}
}
