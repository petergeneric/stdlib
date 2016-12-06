package com.peterphi.servicemanager.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.microsoft.azure.management.dns.models.RecordType;
import com.peterphi.servicemanager.service.db.entity.LetsEncryptCertificateEntity;
import com.peterphi.servicemanager.service.dns.AzureDNS;
import com.peterphi.servicemanager.service.rest.resource.iface.ServiceManagerHostnameRestService;
import com.peterphi.servicemanager.service.rest.resource.type.HostnameRequestDTO;
import com.peterphi.servicemanager.service.rest.resource.type.HostnameResponseDTO;
import com.peterphi.servicemanager.service.ssl.LetsEncryptService;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.types.SimpleId;
import org.apache.commons.lang.StringUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ServiceManagerHostnameRestServiceImpl implements ServiceManagerHostnameRestService
{
	@Inject
	AzureDNS dns;

	@Inject
	LetsEncryptService letsEncrypt;

	@Inject
	@Named("dns.generate.suffix")
	@Doc("The suffix for generated DNS hostnames (e.g. 'mydomain.com')")
	public String dnsSuffix;


	@Override
	public HostnameResponseDTO allocateHostname(final HostnameRequestDTO request)
	{
		// Validate the IP address
		final InetAddress ip;
		try
		{
			ip = InetAddress.getByName(request.ip);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid/malformed IP address: " + request.ip);
		}

		// Optionally allocate a hostname
		final String hostname;
		if (request.hostname != null)
			hostname = request.hostname;
		else
			hostname = generateHostname(request.prefix);

		// Generate an SSL cert
		final LetsEncryptCertificateEntity cert = letsEncrypt.issue(hostname);

		// Set up the hostname
		if (ip instanceof Inet6Address)
			dns.createDNSRecord(hostname, RecordType.AAAA, request.ip);
		else
			dns.createDNSRecord(hostname, RecordType.A, request.ip);

		return serialise(cert);
	}


	@Override
	@Transactional(readOnly = true)
	public HostnameResponseDTO refreshHostname(final String managementToken, final String hostname)
	{
		final LetsEncryptCertificateEntity entity = letsEncrypt.getCertificate(hostname);

		if (entity == null)
			throw new IllegalArgumentException("No such hostname");
		else if (!StringUtils.equals(entity.getManagementToken(), managementToken))
			throw new IllegalArgumentException("Management token mismatch");

		return serialise(entity);
	}


	private HostnameResponseDTO serialise(final LetsEncryptCertificateEntity entity)
	{
		final List<String> hostnames = Arrays.asList(entity.getId().split(","));

		final HostnameResponseDTO response = new HostnameResponseDTO();

		response.primaryHostname = hostnames.get(0);

		if (hostnames.size() > 1)
			response.alternateHostnames = hostnames.subList(1, hostnames.size() - 1);

		response.managementToken = entity.getManagementToken();
		response.sslKeypair = new String(entity.getKeypair(), StandardCharsets.UTF_8);
		response.sslCert = new String(entity.getCert(), StandardCharsets.UTF_8);
		response.sslChain = new String(entity.getChain(), StandardCharsets.UTF_8);

		response.created = entity.getCreated().toDate();
		response.updated = entity.getUpdated().toDate();

		return response;
	}


	private String generateHostname(String prefix)
	{
		// Start with 5 random characters in the hostname
		int randomLength = 3;

		for (int i = 0; i < 100; i++)
		{
			// Slowly increase the search space as we experience failures
			final int randomCharacters = randomLength + (i / 5);

			final String hostname = prefix + "-" + SimpleId.alphanumeric(randomLength).toUpperCase() + "." + dnsSuffix;

			if (letsEncrypt.getCertificate(hostname) == null)
				return hostname; // hostname not used before
		}

		throw new RuntimeException("Unable to generate unique hostname, even with " + randomLength + " random characters!");
	}
}
