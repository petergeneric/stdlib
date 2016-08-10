package com.peterphi.usermanager.db.dao.hibernate;

import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class OAuthServiceDaoImpl extends HibernateDao<OAuthServiceEntity, String>
{
	public OAuthServiceEntity getByClientIdAndEndpoint(final String id, final String redirectUri)
	{
		OAuthServiceEntity entity = getById(id);

		if (entity != null && !entity.isEnabled())
			return null; // If not enabled return nothing

		return filterByEndpoint(entity, redirectUri);
	}


	/**
	 * Checks that one of the registered endpoints is a prefix match for the supplied <code>redirectUri</code>
	 *
	 * @param entity
	 * 		the database entity representing the service
	 * @param redirectUri
	 * 		the redirectUri received from the client
	 *
	 * @return
	 */
	private OAuthServiceEntity filterByEndpoint(OAuthServiceEntity entity, final String redirectUri)
	{
		if (entity != null)
		{
			if (redirectUri == null)
				return entity; // If there's no redirectUri supplied then allow it

			final String[] endpoints = entity.getEndpoints().split("\n");

			for (String endpoint : endpoints)
			{
				if (redirectUri.startsWith(endpoint))
					return entity;
			}
		}

		return null;
	}


	public OAuthServiceEntity getByClientIdAndSecretAndEndpoint(final String id, final String secret, final String redirectUri)
	{
		final Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("id", id));
		criteria.add(Restrictions.eq("enabled", true));
		criteria.add(Restrictions.eq("clientSecret", secret));

		final OAuthServiceEntity entity = uniqueResult(criteria);

		return filterByEndpoint(entity, redirectUri);
	}
}
