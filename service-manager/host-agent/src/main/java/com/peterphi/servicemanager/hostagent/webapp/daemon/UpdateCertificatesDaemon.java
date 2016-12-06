package com.peterphi.servicemanager.hostagent.webapp.daemon;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.servicemanager.hostagent.webapp.service.NginxService;
import com.peterphi.servicemanager.service.rest.resource.iface.ServiceManagerHostnameRestService;
import com.peterphi.servicemanager.service.rest.resource.type.HostnameResponseDTO;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.threading.Timeout;

import java.util.concurrent.TimeUnit;

@EagerSingleton
public class UpdateCertificatesDaemon extends GuiceRecurringDaemon
{
	@Inject
	public Provider<ServiceManagerHostnameRestService> serviceManager;

	@Inject
	public Provider<NginxService> nginx;

	@Inject
	@Named("host-agent.management-token")
	public String managementToken;

	@Inject
	@Named("host-agent.hostname")
	public String hostname;


	public UpdateCertificatesDaemon()
	{
		super(new Timeout(24, TimeUnit.HOURS));
	}


	@Override
	protected void execute() throws Exception
	{
		final ServiceManagerHostnameRestService service = serviceManager.get();

		final HostnameResponseDTO updatedCertificates = service.refreshHostname(managementToken, hostname);

		final NginxService nginx = this.nginx.get();

		// TODO check if the certificates have changed? If they have not we can skip this step
		nginx.installCertificates(updatedCertificates.sslKeypair, updatedCertificates.sslCert, updatedCertificates.sslChain);

		nginx.reload();
	}
}
