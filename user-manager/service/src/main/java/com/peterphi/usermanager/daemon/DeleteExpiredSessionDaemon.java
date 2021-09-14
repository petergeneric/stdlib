package com.peterphi.usermanager.daemon;

import com.google.inject.Inject;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.threading.Timeout;
import com.peterphi.usermanager.db.dao.hibernate.OAuthDelegatedTokenDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.OAuthSessionDaoImpl;
import com.peterphi.usermanager.db.entity.OAuthDelegatedTokenEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionEntity;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

@EagerSingleton
@Doc("Cleans away expired sessions")
public class DeleteExpiredSessionDaemon extends GuiceRecurringDaemon
{
	@Inject
	OAuthSessionDaoImpl sessionDao;

	@Inject
	OAuthDelegatedTokenDaoImpl delegatedTokenDao;


	public DeleteExpiredSessionDaemon()
	{
		super(new Timeout(6, TimeUnit.HOURS));
	}


	@Override
	protected void execute() throws Exception
	{
		// Firstly, delete expired sessions
		final int sessions = deleteExpiredSessions();

		// Now delete expired delegated tokens
		final int tokens = deleteExpiredDelegatedTokens();

		setTextState("Deleted " + tokens + " tokens and " + sessions + " sessions");
	}


	@Transactional
	public int deleteExpiredDelegatedTokens()
	{
		List<OAuthDelegatedTokenEntity> list = delegatedTokenDao.find(findExpiredDelegatedTokens()).getList();

		setTextState("Deleting " + list.size() + " expired delegated tokens");

		for (OAuthDelegatedTokenEntity entity : list)
		{
			delegatedTokenDao.delete(entity);
		}

		return list.size();
	}


	@Transactional
	public int deleteExpiredSessions()
	{
		List<OAuthSessionEntity> list = sessionDao.find(findExpiredSessions()).getList();

		setTextState("Deleting " + list.size() + " expired sessions");

		for (OAuthSessionEntity entity : list)
		{
			sessionDao.delete(entity);
		}

		return list.size();
	}


	private WebQuery findExpiredSessions()
	{
		WebQuery query = new WebQuery();

		query.le("expires", getDeleteCutoff());

		query.limit(0);

		return query;
	}


	private WebQuery findExpiredDelegatedTokens()
	{
		WebQuery query = new WebQuery();

		query.le("expires", DateTime.now());

		query.limit(0);

		return query;
	}


	/**
	 * Delete expired sessions from the database after 14 days<br /> Kept for that time for tracing purposes.<br />
	 *
	 * @return
	 */
	private DateTime getDeleteCutoff()
	{
		// TODO make this configurable?
		return DateTime.now().minusDays(14);
	}
}
