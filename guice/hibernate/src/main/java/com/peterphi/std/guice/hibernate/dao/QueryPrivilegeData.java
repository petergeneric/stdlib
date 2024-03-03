package com.peterphi.std.guice.hibernate.dao;

public record QueryPrivilegeData(boolean permitSchemaPrivateAccess)
{
	/**
	 * Access to {@link com.peterphi.std.guice.database.annotation.WebQueryPrivate} fields is prohibited in fetch/order/constraint (but an entity containing a private field may be fetched back, at which point the caller must take responsibility for filtering private data)
	 */
	public static final QueryPrivilegeData NORMAL = new QueryPrivilegeData(false);

	/**
	 * Access is permitted to {@link com.peterphi.std.guice.database.annotation.WebQueryPrivate} fields. The caller accepts responsibility for controlling access to these private fields
	 */
	public static final QueryPrivilegeData FULL = new QueryPrivilegeData(true);
}
