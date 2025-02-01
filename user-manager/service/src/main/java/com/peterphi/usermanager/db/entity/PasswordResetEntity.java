package com.peterphi.usermanager.db.entity;

import com.peterphi.std.guice.database.annotation.EagerFetch;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "password_reset_code")
public class PasswordResetEntity
{
	private String id;

	private UserEntity user;

	private DateTime expires = DateTime.now().plusHours(24);


	@Id
	public String getId()
	{
		return id;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	@EagerFetch
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	public UserEntity getUser()
	{
		return user;
	}


	public void setUser(final UserEntity user)
	{
		this.user = user;
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
}
