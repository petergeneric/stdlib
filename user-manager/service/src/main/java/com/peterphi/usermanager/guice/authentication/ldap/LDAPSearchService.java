package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import org.apache.commons.lang.StringUtils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Singleton
public class LDAPSearchService
{
	@Inject
	@Named("ldap.domain")
	@Reconfigurable
	public String domain;

	@Inject
	@Named("ldap.endpoint")
	@Reconfigurable
	public String ldapEndpoint;

	@Inject
	@Named("ldap.search-base")
	@Reconfigurable
	public String ldapSearchBase;

	/**
	 * Formatted with 1 argument, the username
	 */
	@Inject
	@Named("ldap.filter")
	@Reconfigurable
	public String ldapFilter;

	/**
	 * Formatted with 1 argument, the user's DN. Searches for the groups a user is a member of (directly or indirectly)
	 */
	@Inject(optional = true)
	@Named("ldap.find-groups-filter")
	@Reconfigurable
	public String ldapGroupFilter = "(member:1.2.840.113556.1.4.1941:=%s)";

	/**
	 * The find pattern to use on group DNs. e.g. <code>(?i)^cn=([^,]+),</code>
	 */
	@Inject
	@Named("ldap.group-regex.find")
	@Reconfigurable
	public String ldapGroupFind;

	/**
	 * The replacement pattern to use after the find pattern is executed - e.g. <code>$1</code> for the first capture group
	 */
	@Inject
	@Named("ldap.group-regex.replace")
	@Reconfigurable
	public String ldapGroupReplace;

	@Inject(optional = true)
	@Named("ldap.group.user-manager-admin")
	@Doc("The group name (after group-regex execution) to treat as builtin group user-manager-admin (ignored if omitted)")
	@Reconfigurable
	public String groupUserManagerAdmin = null;


	@Inject(optional = true)
	@Named("ldap.group.admin")
	@Doc("The group name (after group-regex execution) to treat as builtin group admin (ignored if omitted)")
	@Reconfigurable
	public String groupAdmin = null;

	@Inject(optional = true)
	@Named("ldap.group.framework-admin")
	@Doc("The group name (after group-regex execution) to treat as builtin group framework-admin (ignored if omitted)")
	@Reconfigurable
	public String groupFrameworkAdmin = null;

	@Inject(optional = true)
	@Named("ldap.group.framework-info")
	@Doc("The group name (after group-regex execution) to treat as builtin group framework-info (ignored if omitted)")
	@Reconfigurable
	public String groupFrameworkInfo = null;


	/**
	 * Get the groups that a user is a member of (either directly or indirectly)
	 *
	 * @param ldap
	 * @param dn
	 *
	 * @return
	 *
	 * @throws NamingException
	 */
	private List<LDAPGroup> getGroups(final DirContext ldap, final String dn) throws NamingException
	{
		// Get the DN of all grouos the user is directly/indirectly a member of
		final NamingEnumeration<SearchResult> answer;
		{
			SearchControls search = new SearchControls();

			search.setSearchScope(SearchControls.SUBTREE_SCOPE);
			search.setReturningAttributes(new String[]{"dn"});

			final String searchFilter = String.format(this.ldapGroupFilter, dn);

			answer = ldap.search(ldapSearchBase, searchFilter, search);
		}

		List<LDAPGroup> groups = new ArrayList<>();

		while (answer.hasMoreElements())
		{
			SearchResult sr = answer.next();

			final String groupDN = sr.getNameInNamespace();

			groups.add(dnToLdapGroup(groupDN));
		}

		return groups;
	}


	public LDAPUser parseUser(final String inputUsername)
	{
		// Allow the user to provide domain-slash-user or user@domain in addition to bare "user" with an implied domain
		final String username;
		final String fullyQualifiedUsername;
		if (inputUsername.indexOf('\\') > 0)
		{
			final String[] segments = StringUtils.split(inputUsername, "\\", 2);

			final String domain = segments[0];
			username = segments[1]; // get bare username (discard everything before the slash)

			fullyQualifiedUsername = username + "@" + domain;
		}
		else if (inputUsername.indexOf('@') > 0)
		{
			fullyQualifiedUsername = inputUsername;

			username = StringUtils.split(inputUsername, '@')[0]; // get bare username (discard everything after the @)
		}
		else
		{
			// Implied domain
			fullyQualifiedUsername = inputUsername + "@" + domain;

			username = inputUsername;
		}

		return new LDAPUser(username, fullyQualifiedUsername);
	}


	public LDAPUserRecord search(LDAPUser authUser, final String password, LDAPUser searchFor)
	{
		final Map<LDAPUser, LDAPUserRecord> results = search(authUser, password, Collections.singletonList(searchFor));

		return results.get(searchFor);
	}


	public Map<LDAPUser, LDAPUserRecord> search(LDAPUser authUser, final String password, List<LDAPUser> searchForAll)
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
				ldapEnv.put(Context.SECURITY_PRINCIPAL, authUser.fullyQualifiedUsername);
				ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
				ldapContext = new InitialDirContext(ldapEnv); // N.B. sometimes takes ~10 seconds

				Map<LDAPUser, LDAPUserRecord> results = new HashMap<>();

				// Run a separate search for each user
				for (LDAPUser searchFor : searchForAll)
				{
					final NamingEnumeration<SearchResult> answer;
					{
						SearchControls search = new SearchControls();

						search.setSearchScope(SearchControls.SUBTREE_SCOPE);
						search.setReturningAttributes(new String[]{"dn", "name", "samAccountName"});

						final String searchFilter = String.format(this.ldapFilter, searchFor.username);

						answer = ldapContext.search(ldapSearchBase, searchFilter, search);
					}

					LDAPUserRecord record = null;

					while (answer.hasMoreElements())
					{
						SearchResult sr = answer.next();
						Attributes attrs = sr.getAttributes();

						final String dn = sr.getNameInNamespace();
						final String name = attrs.get("name").get().toString();
						final String actualUsername = attrs.get("samAccountName").get().toString();

						// Get the direct & indirect group membership data
						List<LDAPGroup> groups = getGroups(ldapContext, dn);

						record = new LDAPUserRecord(actualUsername, name, groups);
					}

					results.put(searchFor, record);
				}

				return results;
			}
			finally
			{
				if (ldapContext != null)
					ldapContext.close();
			}
		}
		catch (NamingException e)
		{
			throw new RuntimeException(
					"Error accessing LDAP server (incorrect username/password or server connection issue, please try again)",
					e);
		}
	}


	private LDAPGroup dnToLdapGroup(final String dn)
	{
		String id = dn.replaceAll(ldapGroupFind, ldapGroupReplace);

		// If configured, substitute known LDAP groups for well-known admin groups
		id = replaceGroupName(id, groupFrameworkInfo, "framework-info"); // able to see /guice pages
		id = replaceGroupName(id, groupFrameworkAdmin, "framework-admin"); // able to admin /guice pages
		id = replaceGroupName(id, groupAdmin, "admin"); // application admin
		id = replaceGroupName(id, groupUserManagerAdmin, UserLogin.ROLE_ADMIN); // User Manager admin

		return new LDAPGroup(id, dn);
	}


	private static String replaceGroupName(final String input, final String ifInput, final String replacement)
	{
		if (ifInput != null && StringUtils.equals(ifInput, input))
			return replacement;
		else
			return input;
	}
}
