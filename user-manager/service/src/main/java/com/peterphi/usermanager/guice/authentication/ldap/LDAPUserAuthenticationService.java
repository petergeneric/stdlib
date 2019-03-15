package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.RoleEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LDAPUserAuthenticationService implements UserAuthenticationService
{
	@Inject
	UserDaoImpl dao;

	@Inject
	RoleDaoImpl roleDao;

	@Inject
	LDAPSearchService ldap;


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
		// TODO Authenticate with LDAP and get user record
		LDAPUserRecord record = ldapAuthenticate(username, password);

		// Sync LDAP record into our database
		UserEntity entity = ensureRolesFetched(registerOrUpdateUser(record));

		// Update the last login timestamp
		entity.setLastLogin(DateTime.now());
		dao.update(entity);

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
}
