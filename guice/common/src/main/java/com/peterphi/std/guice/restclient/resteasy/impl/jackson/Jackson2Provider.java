package com.peterphi.std.guice.restclient.resteasy.impl.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.jboss.resteasy.core.messagebody.AsyncBufferedMessageBodyWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class Jackson2Provider extends JacksonJsonProvider implements AsyncBufferedMessageBodyWriter<Object>
{
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static
	{
		OBJECT_MAPPER.findAndRegisterModules();
	}

	public Jackson2Provider()
	{
		super(OBJECT_MAPPER, new Annotations[]{Annotations.JACKSON});
	}
}
