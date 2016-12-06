package com.peterphi.servicemanager.hostagent.webapp.rest.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.servicemanager.hostagent.cmdline.NginxSiteGenerator;
import com.peterphi.servicemanager.hostagent.rest.iface.HostAgentRestService;
import com.peterphi.servicemanager.hostagent.rest.type.DeployUpdatedCerts;
import com.peterphi.servicemanager.hostagent.rest.type.DeployWebappRequest;
import com.peterphi.servicemanager.hostagent.webapp.service.NginxService;
import com.peterphi.servicemanager.hostagent.webapp.service.TomcatService;
import com.peterphi.std.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public class HostAgentRestServiceImpl implements HostAgentRestService
{
	@Inject
	@Named("host-agent.management-token")
	public String managementToken;

	@Inject
	@Named("host-agent.hostname")
	public String hostname;

	@Inject
	NginxService nginx;

	@Inject
	NginxSiteGenerator siteGenerator;

	@Inject
	TomcatService tomcat;

	@Inject(optional = true)
	@Named("host-agent.keypair")
	public File keypairFile = new File("/etc/ssl/private/host.key");

	@Inject(optional = true)
	@Named("host-agent.cert-and-chain")
	public File chainFile = new File("/etc/ssl/certs/host-cert-and-chain.pem");


	@Override
	public void updateCertificates(final DeployUpdatedCerts request)
	{
		checkToken(request.managementToken);

		nginx.installCertificates(request.sslKeypair, request.sslCert, request.sslChain);

		nginx.reload();
	}


	/**
	 * TODO implement me
	 *
	 * @param request
	 */
	@Override
	public void deployWebapp(final DeployWebappRequest request)
	{
		checkToken(request.managementToken);

		// Download the resource to a temporary location
		if (StringUtils.isNotBlank(request.httpEndpoint))
		{
			throw new NotImplementedException("HTTP Endpoint Source functionality not developed yet");
		}
		else
		{
			throw new NotImplementedException("Service Manager Resource Source functionality not developed yet");
		}

		// Optionally check the SHA256 matches

		// Deploy the webapp into the specified tomcat domain
		// N.B. if there's already a webapp deployed we should deploy as a #newversion.war and then undeploy the old webapp when done
	}


	@Override
	public void reindexWebapps()
	{
		// Recreate the nginx site config
		final String config = siteGenerator.render(hostname, chainFile, keypairFile, tomcat.getTomcatsAndPorts());

		nginx.reconfigure(config);

		nginx.reload();
	}


	/**
	 * Throws a {@link RuntimeException} if the management token is not valid
	 *
	 * @param token
	 */
	void checkToken(final String token)
	{
		if (!StringUtils.equals(managementToken, token))
			throw new RuntimeException("Management token provided in call does not match expected value!");
	}
}
