package com.peterphi.usermanager.db.dao.hibernate;

import com.peterphi.usermanager.db.entity.UserEntity;
import com.google.inject.Singleton;
import com.peterphi.std.crypto.BCrypt;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.UUID;

@Singleton
public class UserDaoImpl extends HibernateDao<UserEntity, Integer>
{
	private static final Logger log = Logger.getLogger(UserDaoImpl.class);


	@Transactional
	public UserEntity login(String email, String password)
	{
		final UserEntity account = getUserByEmail(email);

		if (account != null)
		{
			final boolean correct = BCrypt.verify(account.getPassword(), password.toCharArray());

			if (correct)
			{
				account.setLastLogin(new DateTime());
				update(account);
				return account;
			}
		}

		return null; // User doesn't exist (or password is wrong)
	}


	protected UserEntity getUserByEmail(String email)
	{
		final Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("email", email));

		return uniqueResult(criteria);
	}


	public void changePassword(final int id, final String newPassword)
	{
		final UserEntity account = getById(id);

		if (account != null)
		{
			account.setPassword(hashPassword(newPassword));

			update(account);
		}
		else
		{
			throw new IllegalArgumentException("No such user: " + id);
		}
	}


	/**
	 * Creates a BCrypted hash for a password
	 *
	 * @param password
	 *
	 * @return
	 */
	private String hashPassword(String password)
	{
		return BCrypt.hash(password.toCharArray(), BCrypt.DEFAULT_COST);
	}


	@Transactional
	public int register(String name,
	                    String email,
	                    String password,
	                    final String dateFormat,
	                    final String timeZone)
	{
		if (userExists(email))
			throw new IllegalArgumentException("User with e-mail address " + email + " already exists!");
		if (password.isEmpty())
			throw new IllegalArgumentException("Must supply a password!");

		final UserEntity account = new UserEntity();

		account.setName(name);
		account.setEmail(email);
		account.setPassword(hashPassword(password));
		account.setDateFormat(dateFormat);
		account.setTimeZone(timeZone);
		account.setCreated(new DateTime());
		account.setSessionReconnectKey(UUID.randomUUID().toString());

		return save(account);
	}


	@Transactional
	protected boolean userExists(String email)
	{
		final UserEntity account = getUserByEmail(email);

		return (account != null);
	}


	@Transactional
	public UserEntity loginBySessionReconnectKey(String key)
	{
		final Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("sessionReconnectKey", key));

		criteria.setMaxResults(1);

		final UserEntity account = uniqueResult(criteria);

		if (account != null)
		{
			log.info("Allowed login by session reconnect key for user: " + account.getEmail());

			account.setLastLogin(new DateTime());
			update(account);
		}

		return account;
	}


	@Transactional
	public void changeSessionReconnectKey(Integer id)
	{
		final UserEntity account = getById(id);

		if (account != null)
		{
			account.setSessionReconnectKey(UUID.randomUUID().toString());
			update(account);
		}
	}


	@Transactional
	public UserEntity changeProfile(final int id, final String name, final String email, final String dateFormat, final String timeZone)
	{
		final UserEntity account = getById(id);

		if (account != null)
		{
			if (dateFormat == null || timeZone == null)
				throw new IllegalArgumentException("Must specify dateFormat and timeZone!");

			account.setName(name);
			account.setEmail(email);
			account.setDateFormat(dateFormat);
			account.setTimeZone(timeZone);

			update(account);

			return account;
		}
		else
		{
			throw new IllegalArgumentException("No such user: " + id);
		}
	}
}
