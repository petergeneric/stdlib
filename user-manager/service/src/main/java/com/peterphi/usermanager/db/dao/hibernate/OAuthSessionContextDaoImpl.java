package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionContextEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

@Singleton
public class OAuthSessionContextDaoImpl extends HibernateDao<OAuthSessionContextEntity, Integer>
{
	@Transactional(readOnly = true)
	public OAuthSessionContextEntity get(final int userId, final String serviceId, final String scope)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("user.id", userId));
		criteria.add(Restrictions.eq("service.id", serviceId));
		criteria.add(Restrictions.eq("scope", StringUtils.trimToEmpty(scope)));
		criteria.add(Restrictions.eq("active", true));

		return uniqueResult(criteria);
	}


	/**
	 * Create a context (or reuse an existing active context)
	 *
	 * @param user
	 * 		the user
	 * @param service
	 * 		the service
	 * @param scope
	 * 		the access scope the service is granted
	 *
	 * @return
	 */
	@Transactional
	public OAuthSessionContextEntity create(final UserEntity user, final OAuthServiceEntity service, final String scope)
	{
		// Create a new context
		OAuthSessionContextEntity entity = get(user.getId(), service.getId(), scope);

		// No active context exists, we must create one
		if (entity == null)
		{
			entity = new OAuthSessionContextEntity();

			entity.setUser(user);
			entity.setScope(StringUtils.trimToEmpty(scope));
			entity.setService(service);
			entity.setActive(true);

			entity.setId(save(entity));
		}

		return entity;
	}
}
