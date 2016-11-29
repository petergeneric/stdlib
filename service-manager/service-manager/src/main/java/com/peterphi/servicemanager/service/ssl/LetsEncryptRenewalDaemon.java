package com.peterphi.servicemanager.service.ssl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.servicemanager.service.db.dao.impl.LetsEncryptCertificateDaoImpl;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

@Doc("Responsible for automatic renewal of Let's Encrypt certificate")
@EagerSingleton
public class LetsEncryptRenewalDaemon extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(LetsEncryptRenewalDaemon.class);

	@Inject
	LetsEncryptService letsEncrypt;

	@Inject
	LetsEncryptCertificateDaoImpl dao;

	@Inject(optional = true)
	@Named("service.LetsEncryptRenewalDaemon.enabled")
	public boolean enabled = true;

	@Inject(optional = true)
	@Named("acmeca.renew-days")
	@Doc("The number of days ahead of expiry to renew certs")
	public int renewDays = 30;


	protected LetsEncryptRenewalDaemon()
	{
		super(new Timeout(1, TimeUnit.DAYS));
	}


	@Override
	protected void execute() throws Exception
	{
		if (!enabled)
			return;

		for (String domain : dao.getEligibleForRenewal(renewDays))
		{
			try
			{
				renew(domain);
			}
			catch (Throwable t)
			{
				log.error("Error renewing cert for " + domain + ": " + t.getMessage(), t);
			}
		}
	}


	private void renew(final String domain)
	{
		letsEncrypt.renew(domain);
	}
}
