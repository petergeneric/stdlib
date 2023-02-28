package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.types.SimpleId;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import com.peterphi.usermanager.util.UserManagerBearerToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OAuthServiceDaoImpl extends HibernateDao<OAuthServiceEntity, String>
{
	private static final Logger log = LoggerFactory.getLogger(OAuthServiceDaoImpl.class);


	public OAuthServiceEntity getByClientIdAndEndpoint(final String id, final String redirectUri)
	{
		if (log.isDebugEnabled())
			log.debug("Search for service by id: " + id);

		OAuthServiceEntity entity = getById(id);

		if (entity != null && !entity.isEnabled())
		{
			log.warn("getByClientIdAndEndpoint called for disabled service " + id + " with redirectUri " + redirectUri);
			return null; // If not enabled return nothing
		}

		if (entity == null)
			log.warn("No service with id: " + id);

		return filterByEndpoint(entity, redirectUri);
	}


	/**
	 * Checks that one of the registered endpoints is a prefix match for the supplied <code>redirectUri</code>
	 *
	 * @param entity      the database entity representing the service
	 * @param redirectUri the redirectUri received from the client
	 * @return
	 */
	private OAuthServiceEntity filterByEndpoint(OAuthServiceEntity entity, final String redirectUri)
	{
		if (entity != null)
		{
			if (redirectUri == null)
				return entity; // If there's no redirectUri supplied then allow it

			// N.B. database can have \r\n so we need to normalise this
			final List<String> endpoints = Arrays
					                               .stream(StringUtils
							                                       .trimToEmpty(entity.getEndpoints())
							                                       .replace('\r', '\n')
							                                       .split("\n"))
					                               .map(StringUtils :: trimToEmpty)
					                               .filter(s -> s.length() > 0)
					                               .collect(Collectors.toList());

			if (log.isDebugEnabled())
				log.debug("Check endpoint " + redirectUri + " against " + Arrays.asList(endpoints));

			// Now check if the redirectUri matches any of the registered endpoints
			// N.B. don't allow arbitrary startsWith (security issue if the redirectUri can be pointed at a resource in the app that redirects elsewhere)
			for (String endpoint : endpoints)
			{
				if (StringUtils.equals(endpoint, redirectUri))
				{
					if (log.isDebugEnabled())
						log.debug("Exact match to endpoint: " + endpoint);

					return entity; // exact match
				}
				else
				{
					// Allow database to hold a base service endpoint and for the client to supply the oauth2 callback resource of that service
					final String withCallbackEndpointAdded = endpoint + (endpoint.endsWith("/") ? "" : "/") + "oauth2/client/cb";

					if (StringUtils.equals(withCallbackEndpointAdded, redirectUri))
					{
						if (log.isDebugEnabled())
							log.debug("Match to endpoint with added /oauth2/client/cb suffix: " + endpoint);

						return entity;
					}
				}
			}
		}

		// Default: no match found
		return null;
	}


	public OAuthServiceEntity getByClientIdAndSecretAndEndpoint(final String id, final String secret, final String redirectUri)
	{
		final OAuthServiceEntity entity = getByClientIdAndSecretOnly(id, secret);

		return filterByEndpoint(entity, redirectUri);
	}


	public OAuthServiceEntity getByClientIdAndSecretOnly(final String id, final String secret)
	{
		return uniqueResult(new WebQuery().eq("id", id).eq("enabled", true).eq("clientSecret", secret));
	}


	public void rotateUserAccessKey(final OAuthServiceEntity entity)
	{
		if (entity == null)
			throw new IllegalArgumentException("No service provided!");

		// Set the secondary token to the old primary token
		entity.setAccessKeySecondary(entity.getAccessKey());

		// Now regenerate the primary token
		entity.setAccessKey(SimpleId.alphanumeric(UserManagerBearerToken.PREFIX_SERVICE_TOKEN, 100));

		update(entity);
	}


	public OAuthServiceEntity getByAccessKey(final String key)
	{
		return find(new WebQuery()
				            .limit(0)
				            .eq("enabled", true)
				            .or(or -> or.eq("accessKey", key).eq("accessKeySecondary", key))).uniqueResult();
	}
}
