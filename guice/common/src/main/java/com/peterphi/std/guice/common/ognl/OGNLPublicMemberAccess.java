package com.peterphi.std.guice.common.ognl;

// N.B. implementation from ognl 3.2.1 which is the last version to include this class

import ognl.MemberAccess;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * This class provides methods for setting up and restoring access in a Field.  Java 2 provides access utilities for setting and
 * getting fields that are non-public.  This object provides coarse-grained access controls to allow access to private, protected
 * and package protected members.  This will apply to all classes and members.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 * @version 15 October 1999
 */
class OGNLPublicMemberAccess implements MemberAccess
{

	public Object setup(Map context, Object target, Member member, String propertyName)
	{
		return null;
	}


	public void restore(Map context, Object target, Member member, String propertyName, Object state)
	{
	}


	@Override
	public boolean isAccessible(final Map context, final Object target, final Member member, final String propertyName)
	{
		return Modifier.isPublic(member.getModifiers());
	}
}
