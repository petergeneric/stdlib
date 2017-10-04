package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.crypto.BCrypt;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.usermanager.db.entity.UserEntity;
import org.apache.log4j.Logger;
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

		if (account != null && account.isLocal())
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


	public boolean isUserLocal(final String email)
	{
		UserEntity record = getUserByEmail(email);

		return (record != null && record.isLocal());
	}


	public UserEntity getUserByEmail(String email)
	{
		return uniqueResult(new WebQuery().eq("email", email));
	}


	public void changePassword(final int id, final String newPassword)
	{
		final UserEntity account = getById(id);

		if (account != null)
		{
			if (!account.isLocal())
				throw new IllegalArgumentException("Cannot change password: user is authenticated by remote service!");

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
	public int register(String name, String email, String password, final String dateFormat, final String timeZone)
	{
		if (userExists(email))
			throw new IllegalArgumentException("User '" + email + "' already exists!");
		if (password.isEmpty())
			throw new IllegalArgumentException("Must supply a password!");

		final UserEntity account = new UserEntity();

		account.setLocal(true);
		account.setName(name);
		account.setEmail(email);
		account.setPassword(hashPassword(password));
		account.setDateFormat(dateFormat);
		account.setTimeZone(timeZone);
		account.setSessionReconnectKey(UUID.randomUUID().toString());

		return save(account);
	}


	@Transactional
	public int registerRemote(final String username, final String fullName)
	{
		if (userExists(username))
			throw new IllegalArgumentException("User '" + username + "' already exists!");

		final UserEntity account = new UserEntity();
		account.setLocal(false);
		account.setEmail(username);
		account.setName(fullName);
		account.setPassword("NONE"); // Won't allow password logins anyway, but we also set a value that won't match any BCrypt hash
		account.setSessionReconnectKey(null);

		account.setTimeZone(CurrentUser.DEFAULT_TIMEZONE);
		account.setDateFormat(CurrentUser.DEFAULT_DATE_FORMAT_STRING);

		return save(account);
	}


	@Transactional
	public boolean userExists(String email)
	{
		final UserEntity account = getUserByEmail(email);

		return (account != null);
	}


	@Transactional
	public UserEntity loginBySessionReconnectKey(String key)
	{
		final UserEntity account = uniqueResult(new WebQuery().eq("local", true).eq("sessionReconnectKey", key));

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
			if (account.isLocal())
				account.setSessionReconnectKey(UUID.randomUUID().toString());
			else
				account.setSessionReconnectKey(null);

			update(account);
		}
	}


	@Transactional
	public UserEntity changeProfile(final int id,
	                                final String name,
	                                final String email,
	                                final String dateFormat,
	                                final String timeZone)
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
