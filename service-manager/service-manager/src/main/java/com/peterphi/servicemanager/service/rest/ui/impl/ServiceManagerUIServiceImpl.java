package com.peterphi.servicemanager.service.rest.ui.impl;

import com.google.inject.Inject;
import com.peterphi.servicemanager.service.guice.LowSecuritySessionNonceStore;
import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import com.peterphi.servicemanager.service.logging.LogStore;
import com.peterphi.servicemanager.service.logging.hub.LogSubscriber;
import com.peterphi.servicemanager.service.logging.hub.LoggingService;
import com.peterphi.servicemanager.service.rest.ui.api.ServiceManagerUIService;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.std.types.SimpleId;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@SessionScoped
public class ServiceManagerUIServiceImpl implements ServiceManagerUIService
{
	private static final Logger log = Logger.getLogger(ServiceManagerUIServiceImpl.class);
	private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");

	@Inject
	Templater templater;

	@Inject
	LowSecuritySessionNonceStore nonceStore;

	@Inject
	CurrentUser user;

	@Inject
	LogStore logStore;

	@Inject
	LogSerialiser serialiser;

	@Inject
	LoggingService loggingService;

	private Map<String, LogSubscriber> subscribers = new HashMap<>();


	@Override
	public Response getIndex()
	{
		return Response.seeOther(URI.create("/resources")).build();
	}


	@Override
	public String getTail()
	{
		final TemplateCall call = templater.template("tail");

		// Create a new subscription to the log stream
		// Also take this opportunity to remove purged subscriptions from the map
		final String subscriptionId = user.getUsername() + "_" + SimpleId.alphanumeric(10);

		synchronized (subscribers)
		{
			log.info("Created log tail subscription " + subscriptionId);

			subscribers.put(subscriptionId, loggingService.subscribe(new LogSubscriber(subscriptionId)));

			// Remove purged subscribers
			final Iterator<Map.Entry<String, LogSubscriber>> it = subscribers.entrySet().iterator();
			while (it.hasNext())
			{
				if (it.next().getValue().isPurged())
					it.remove();
			}
		}

		call.set("nonce", nonceStore.getValue());
		call.set("subscriptionId", subscriptionId);

		return call.process();
	}


	@Override
	public String getSearchLogs(String from, String to, final int minLevel)
	{
		if (!logStore.isSearchSupported())
			throw new IllegalArgumentException("The underlying log store does not support web-based searching!");

		final TemplateCall call = templater.template("search");

		call.set("nonce", nonceStore.getValue());
		call.set("fromDatetime", from);
		call.set("toDatetime", to);
		call.set("minLevel", minLevel);

		return call.process();
	}


	@Override
	public Response doTail(final String subscriptionId)
	{
		LogSubscriber subscriber;

		synchronized (subscribers)
		{
			subscriber = subscribers.get(subscriptionId);
		}

		if (subscriber == null || subscriber.isPurged())
		{
			// If this subscription existed but has expired then we should fail
			throw new IllegalArgumentException("The named log tail subscription " +
			                                   subscriptionId +
			                                   " has expired and been purged by the system. Please refresh the page.");
		}
		else
		{
			final SortedSet<LogLineTableEntity> recent = subscriber.poll();

			// Return the log lines that have appeared since the user last checked in
			// TODO allow a filter to be supplied by the client to reduce volume of data being returned?
			return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(serialiser.toJson(recent)).build();
		}
	}


	@Override
	public Response doSearchLogs(String fromStr, String toStr, String filter)
	{
		DateTime from = DateTime.parse(fromStr).withZone(LONDON); // TODO take into account the user's timezone!
		DateTime to = DateTime.parse(fromStr).withZone(LONDON); // TODO take into account the user's timezone!

		final List<LogLineTableEntity> results = logStore.search(from, to, filter);

		return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(serialiser.toJson(results)).build();
	}
}
