package com.peterphi.usermanager.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "user")
public class UserManagerUser
{
	@XmlAttribute(required = true)
	public Integer id;

	/**
	 * User's real name
	 */
	@XmlElement
	public String name;

	/**
	 * User's e-mail address
	 */
	@XmlElement
	public String email;

	/**
	 * The user's desired date format
	 */
	@XmlElement
	public String dateFormat;

	/**
	 * The user's desired timezone
	 */
	@XmlElement
	public String timeZone;

	@XmlElementWrapper(name = "roles")
	@XmlElement(name = "role")
	public Set<String> roles = new HashSet<>();


	@XmlElement
	public Date created;
}
