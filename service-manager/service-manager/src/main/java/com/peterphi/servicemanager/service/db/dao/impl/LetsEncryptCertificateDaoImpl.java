package com.peterphi.servicemanager.service.db.dao.impl;

import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.LetsEncryptCertificateEntity;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.List;

@Singleton
public class LetsEncryptCertificateDaoImpl extends HibernateDao<LetsEncryptCertificateEntity, String>
{
	@Transactional(readOnly = true)
	public List<String> getEligibleForRenewal(int renewDays)
	{
		final Criteria criteria = createCriteria();

		criteria.add(Restrictions.lt("expires", DateTime.now().plusDays(renewDays)));
		criteria.addOrder(Order.asc("expires"));

		return getIdList(criteria);
	}
}
