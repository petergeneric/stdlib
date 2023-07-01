package org.thymeleaf.standard.expression;

import ognl.AbstractMemberAccess;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

class OGNLPublicMemberAccess extends AbstractMemberAccess {
	@Override
	public boolean isAccessible(final Map context, final Object target, final Member member, final String propertyName) {
		return Modifier.isPublic(member.getModifiers());
	}
}
