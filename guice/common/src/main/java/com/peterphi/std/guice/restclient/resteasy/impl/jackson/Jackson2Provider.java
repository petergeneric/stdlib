package com.peterphi.std.guice.restclient.resteasy.impl.jackson;

import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.jackson.JacksonFactory;
import org.jboss.resteasy.core.messagebody.AsyncBufferedMessageBodyWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class Jackson2Provider extends JacksonJsonProvider implements AsyncBufferedMessageBodyWriter<Object>
{
	@Inject
	public Jackson2Provider(JacksonFactory factory)
	{
		super(factory.getObjectMapper(), new Annotations[]{Annotations.JACKSON});
	}
}
