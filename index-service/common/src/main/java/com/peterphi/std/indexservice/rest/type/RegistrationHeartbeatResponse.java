package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name="registrationHeartbeatResponse")
public class RegistrationHeartbeatResponse
{
	/**
	 * If true then the recipient must perform a full re-register
	 */
	@XmlAttribute(required = true)
	public Boolean mustReregister;

	/**
	 * The date when the next heartbeat is expected by<br />
	 * If set the client should attempt to send the next heartbeat by the given timestamp
	 */
	@XmlAttribute(required = false)
	public Date nextHeartbeatExpectedBy;
}
