package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.usermanager.db.entity.WebAuthnCredentialEntity;

@Singleton
public class WebAuthnCredentialDaoImpl extends HibernateDao<WebAuthnCredentialEntity, String>
{
}
