package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.RoleEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import org.joda.time.DateTime;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LDAPUserAuthenticationService implements UserAuthenticationService
{
	@Inject
	UserDaoImpl dao;

	@Inject
	RoleDaoImpl roleDao;

	@Inject
	@Named("ldap.domain")
	public String domain;

	@Inject
	@Named("ldap.endpoint")
	public String ldapEndpoint;

	@Inject
	@Named("ldap.search-base")
	public String ldapSearchBase;

	/**
	 * Formatted with 1 argument, the username
	 */
	@Inject
	@Named("ldap.filter")
	public String ldapFilter;

	/**
	 * The find pattern to use on group DNs. e.g. <code>(?i)^cn=([^,]+),</code>
	 */
	@Inject
	@Named("ldap.group-regex.find")
	public String ldapGroupFind;

	/**
	 * The replacement pattern to use after the find pattern is executed - e.g. <code>$1</code> for the first capture group
	 */
	@Inject
	@Named("ldap.group-regex.replace")
	public String ldapGroupReplace;


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


	private LDAPUserRecord ldapAuthenticate(final String username, final String password)
	{
		try
		{
			DirContext ldapContext = null;
			try
			{
				Hashtable<String, String> ldapEnv = new Hashtable<>();
				ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				ldapEnv.put(Context.PROVIDER_URL, ldapEndpoint);
				ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
				ldapEnv.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
				ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
				ldapContext = new InitialDirContext(ldapEnv); // N.B. sometimes takes ~10 seconds

				final NamingEnumeration<SearchResult> answer;
				{
					SearchControls search = new SearchControls();

					search.setSearchScope(SearchControls.SUBTREE_SCOPE);
					search.setReturningAttributes(new String[]{"name", "samAccountName", "memberOf"});

					final String searchFilter = String.format(this.ldapFilter, username);

					answer = ldapContext.search(ldapSearchBase, searchFilter, search);
				}

				while (answer.hasMoreElements())
				{
					SearchResult sr = answer.next();
					Attributes attrs = sr.getAttributes();

					final String name = attrs.get("name").get().toString();
					final String actualUsername = attrs.get("samAccountName").get().toString();

					List<LDAPGroup> groups = Collections
							                         .list(attrs.get("memberOf").getAll())
							                         .stream()
							                         .map(o -> o.toString())
							                         .map(this :: dnToLdapGroup)
							                         .collect(Collectors.toList());

					return new LDAPUserRecord(actualUsername, name, groups);
				}

				return null;
			}
			finally
			{
				if (ldapContext != null)
					ldapContext.close();
			}
		}
		catch (NamingException e)
		{
			throw new RuntimeException("Error accessing LDAP server", e);
		}
	}


	private LDAPGroup dnToLdapGroup(final String dn)
	{
		final String id = dn.replaceAll(ldapGroupFind, ldapGroupReplace);

		return new LDAPGroup(id, dn);
	}
}
