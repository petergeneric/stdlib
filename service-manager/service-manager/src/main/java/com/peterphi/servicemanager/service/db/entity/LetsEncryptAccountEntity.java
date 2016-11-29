package com.peterphi.servicemanager.service.db.entity;

import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity(name = "acme_ca_account")
public class LetsEncryptAccountEntity
{
	/**
	 * The database id to use for the main account id; this may change in the future if there are multiple accounts
	 */
	public static final int MAIN_ACCOUNT_ID = 1;

	private int id = 1;
	private byte[] keypair;

	private DateTime created = new DateTime();
	private DateTime updated = new DateTime();


	@Id
	public int getId()
	{
		return id;
	}


	public void setId(final int id)
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
