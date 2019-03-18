package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.threading.Timeout;
import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.RoleEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LDAPUserAuthenticationService implements UserAuthenticationService
{
	private static final Logger log = Logger.getLogger(LDAPUserAuthenticationService.class);

	@Inject
	UserDaoImpl dao;

	@Inject
	RoleDaoImpl roleDao;

	@Inject
	LDAPSearchService ldap;

	@Inject(optional = true)
	@Named("ldap.background-role-updates")
	@Doc("If true, LDAP role data will be updated in the background")
	public boolean autoRefreshUserData = false;

	@Inject(optional = true)
	@Named("ldap.user-manager-account.username")
	@Doc("The LDAP user to use for background updates")
	public String autoRefreshUsername;

	@Inject(optional = true)
	@Named("ldap.user-manager-account.password")
	@Doc("The LDAP password to use for background updates")
	public String autoRefreshPassword;

	/**
	 * The time we last attempted an opportunistic user data refresh
	 */
	private long lastOpportunisticUserDataRefresh = 0;

	private static final long OPPORTUNISTIC_USER_DATA_REFRESH_THROTTLE = new Timeout(12, TimeUnit.HOURS).getMilliseconds();


	@Override
	@Transactional
	public UserEntity getById(final int id)
	{
		return ensureRolesFetched(dao.getById(id));
	}


	/**
	 * Make sure the roles have been fetched from the database
	 *
	 * @param user
	 *
	 * @return
	 */
	private UserEntity ensureRolesFetched(final UserEntity user)
	{
		if (user != null)
			user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toList());

		return user;
	}


	@Override
	@Transactional
	public UserEntity authenticate(final String username, final String password, final boolean basicAuth)
	{
		// Authenticate with LDAP and get user record
		LDAPUserRecord record = ldapAuthenticate(username, password);

		// Sync LDAP record into our database
		UserEntity entity = ensureRolesFetched(registerOrUpdateUser(record));

		// Update the last login timestamp
		entity.setLastLogin(DateTime.now());
		dao.update(entity);

		// If we're to periodically update user data (but don't have credentials) then consider borrowing these credentials for an update run
		if (shouldOpportunisticallyRefreshUserData())
		{
			opportunisticallyRefreshUserData(username, password);
		}

		return entity;
	}


	private UserEntity registerOrUpdateUser(final LDAPUserRecord ldap)
	{
		UserEntity existing = dao.getUserByEmail(ldap.username);

		if (existing == null)
		{
			dao.registerRemote(ldap.username, ldap.fullName);

			existing = dao.getUserByEmail(ldap.username);

			assert (existing != null);
		}

		// Now sync the role information
		setRoles(existing, ldap);

		return existing;
	}


	private void setRoles(final UserEntity existing, final LDAPUserRecord ldap)
	{
		// First, figure out which roles are new by excluding those that have been removed + those that are unchanged
		// At the same time, remove the roles the user no longer has
		Set<String> noChange = new HashSet<>(); // PKs for roles with no change
		{
			Set<RoleEntity> toRemove = new HashSet<>(); // role entities to remove
			for (RoleEntity roleEntity : existing.getRoles())
			{
				if (ldap.getRoleIds().contains(roleEntity.getId()))
					noChange.add(roleEntity.getId()); // User still has this role
				else
				{
					toRemove.add(roleEntity); // User no longer has this role
					roleEntity.getMembers().remove(existing); // remove us from the role's membership list
				}
			}

			// Remove the roles the user no longer has
			existing.getRoles().removeAll(toRemove);
		}

		// Add the new roles (creating them if necessary)
		for (LDAPGroup group : ldap.roles)
		{
			if (!noChange.contains(group.id))
			{
				RoleEntity role = roleDao.getOrCreate(group.id, "LDAP: " + group.dn);

				role.getMembers().add(existing);
				existing.getRoles().add(role);
			}
		}

		dao.update(existing);
	}


	@Override
	public UserEntity authenticate(final String sessionReconnectToken)
	{
		return null; // unsupported
	}


	private LDAPUserRecord ldapAuthenticate(String inputUsername, final String password)
	{
		final LDAPUser user = ldap.parseUser(inputUsername);

		return ldap.search(user, password, user);
	}


	public boolean shouldOpportunisticallyRefreshUserData()
	{
		return autoRefreshUserData &&
		       autoRefreshUsername == null &&
		       (lastOpportunisticUserDataRefresh + OPPORTUNISTIC_USER_DATA_REFRESH_THROTTLE) < System.currentTimeMillis();
	}


	/**
	 * Temporarily borrow a user's credentials to run an opportunistic user data refresh
	 *
	 * @param username
	 * @param password
	 */
	public synchronized void opportunisticallyRefreshUserData(final String username, final String password)
	{
		if (shouldOpportunisticallyRefreshUserData())
		{
			this.lastOpportunisticUserDataRefresh = System.currentTimeMillis();
			Thread thread = new Thread(() -> refreshAllUserData(ldap.parseUser(username), password, true));
			thread.setDaemon(true);
			thread.setName("UserManager_OpportunisticRefresh");
			thread.start();
		}
	}


	@Override
	@Transactional
	public void executeBackgroundTasks()
	{
		if (autoRefreshUserData)
		{
			final LDAPUser authUser = ldap.parseUser(autoRefreshUsername);
			final String password = autoRefreshPassword;

			refreshAllUserData(authUser, password, false);
		}
	}


	@Transactional
	public void refreshAllUserData(final LDAPUser authUser, final String password, final boolean opportunistic)
	{
		final ConstrainedResultSet<UserEntity> users = dao.find(new WebQuery().limit(0).eq("local", false));

		// Search for all users
		Map<LDAPUser, LDAPUserRecord> results = ldap.search(authUser,
		                                                    password,
		                                                    users
				                                                    .getList()
				                                                    .stream()
				                                                    .map(UserEntity :: getEmail)
				                                                    .map(ldap :: parseUser)
				                                                    .collect(Collectors.toList()));

		for (UserEntity entity : users.getList())
		{
			try
			{
				final LDAPUser searchFor = ldap.parseUser(entity.getEmail());

				LDAPUserRecord record = results.get(searchFor);

				if (record != null)
				{
					// Update their full name
					entity.setName(record.fullName);

					// Update the role data
					setRoles(entity, record);
				}
				else
				{
					if (opportunistic)
					{
						log.warn("Cannot find user in LDAP system: " +
						         entity.getEmail() +
						         " - but check was opportunistic so won't delete the user.");
					}
					else
					{
						// User has been deleted from LDAP so we shouldn't keep a record for them
						// However, the user may have services so we don't necessarily want to remove those, so we'll just make the user unable to log in without a reauth
						log.warn("User no longer known to LDAP system: " + entity.getEmail() + " - deleting User Manager record");

						entity.setSessionReconnectKey(null);
						entity.setAccessKey(null);
						entity.setAccessKeySecondary(null);
						
						dao.update(entity);
					}
				}
			}
			catch (Throwable t)
			{
				log.error("Error updating LDAP record for " + entity.getEmail(), t);
			}
		}
	}
}
