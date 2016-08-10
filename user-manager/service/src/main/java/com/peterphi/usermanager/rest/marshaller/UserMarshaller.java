package com.peterphi.usermanager.rest.marshaller;

import com.google.inject.Singleton;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.stream.Collectors;

@Singleton
public class UserMarshaller
{
	public UserManagerUser marshal(final UserEntity user)
	{
		UserManagerUser obj = new UserManagerUser();

		obj.id = user.getId();
		obj.name = user.getName();
		obj.email = user.getEmail();

		obj.roles = user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet());

		obj.dateFormat = user.getDateFormat();
		obj.timeZone = user.getTimeZone();

		obj.created = marshal(user.getCreated());

		return obj;
	}


	private Date marshal(DateTime date)
	{
		if (date == null)
			return null;
		else
			return date.toDate();
	}
}
