package com.peterphi.servicemanager.service.db.entity;

import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity(name = "acme_ca_cert")
public class LetsEncryptCertificateEntity
{
	private String id;
	private byte[] keypair;
	private byte[] cert;
	private byte[] chain;
	private String managementToken;
	private DateTime expires;
	private DateTime created = DateTime.now();
	private DateTime updated;


	@Id
	@Column(name = "id", length = 8192)
	public String getId()
	{
		return id;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	@Column(name = "keypair_utf8", nullable = false)
	@Lob
	public byte[] getKeypair()
	{
		return keypair;
	}


	public void setKeypair(final byte[] keypair)
	{
		this.keypair = keypair;
	}


	@Column(name = "cert_utf8", nullable = false)
	@Lob
	public byte[] getCert()
	{
		return cert;
	}


	public void setCert(final byte[] cert)
	{
		this.cert = cert;
	}


	@Column(name = "chain_utf8", nullable = false)
	@Lob
	public byte[] getChain()
	{
		return chain;
	}


	public void setChain(final byte[] chain)
	{
		this.chain = chain;
	}


	@Column(name = "mgmt_token", nullable = true, length = 32)
	public String getManagementToken()
	{
		return managementToken;
	}


	public void setManagementToken(final String managementToken)
	{
		this.managementToken = managementToken;
	}


	@Column(name = "expires_ts", nullable = false)
	public DateTime getExpires()
	{
		return expires;
	}


	public void setExpires(final DateTime expires)
	{
		this.expires = expires;
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
