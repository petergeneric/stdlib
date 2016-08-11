package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.types.SimpleId;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionContextEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

@Singleton
public class OAuthSessionDaoImpl extends HibernateDao<OAuthSessionEntity, String>
{
	@Transactional
	public OAuthSessionEntity create(final OAuthSessionContextEntity context, final String initiator, final DateTime expires)
	{
		OAuthSessionEntity session = new OAuthSessionEntity();
		session.setId(SimpleId.alphanumeric("ses-", 36));
		session.setContext(context);
		session.setInitiator(initiator);

		session.setAuthorisationCode(SimpleId.alphanumeric("authc-", 36));

		session.setExpires(expires);

		save(session);

		return getById(session.getId());
	}


	@Transactional
	public OAuthSessionEntity exchangeRefreshTokenForNewToken(final OAuthServiceEntity service,
	                                                          final String refreshToken,
	                                                          final DateTime newExpires)
	{
		Criteria criteria = createCriteria();

		criteria.createAlias("context.service", "service");
		criteria.add(Restrictions.eq("id", refreshToken));
		criteria.add(Restrictions.isNotNull("token"));
		criteria.add(Restrictions.eq("alive", true));
		criteria.add(Restrictions.eq("service.id", service.getId()));

		final OAuthSessionEntity session = uniqueResult(criteria);

		if (session == null)
			return null;

		session.setToken(SimpleId.alphanumeric("tok-", 36));
		session.setExpires(newExpires);

		update(session);

		return session;
	}


	@Transactional
	public OAuthSessionEntity exchangeCodeForToken(final OAuthServiceEntity service, final String authorisationCode)
	{
		Criteria criteria = createCriteria();

		criteria.createAlias("context.service", "service");
		criteria.add(Restrictions.eq("authorisationCode", authorisationCode));
		criteria.add(Restrictions.isNull("token"));
		criteria.add(Restrictions.eq("alive", true));
		criteria.add(Restrictions.eq("service.id", service.getId()));

		final OAuthSessionEntity session = uniqueResult(criteria);

		if (session == null)
			return null;

		session.setToken(SimpleId.alphanumeric("tok-", 36));
		session.setAuthorisationCode(null);

		update(session);

		return session;
	}


	@Transactional
	public OAuthSessionEntity extend(final String refreshToken, final DateTime newExpires)
	{
		OAuthSessionEntity session = getById(refreshToken);

		if (refreshToken == null)
			throw new IllegalArgumentException("Invalid refresh token: no live session by id  " + refreshToken);
		else if (!session.isAlive())
			throw new IllegalArgumentException("Invalid refresh token: no live session by id  " + refreshToken);

		session.setExpires(newExpires);
		session.setToken(SimpleId.alphanumeric("tkn-", 36));

		update(session);

		return session;
	}


	@Transactional(readOnly = true)
	public OAuthSessionEntity getByToken(final String token)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("token", token));
		criteria.add(Restrictions.eq("alive", true));
		criteria.add(Restrictions.gt("expires", DateTime.now()));

		return uniqueResult(criteria);
	}
}
