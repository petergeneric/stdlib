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
import com.peterphi.std.types.SimpleId;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

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
		final InetAddress ip;
		try
		{
			ip = InetAddress.getByName(request.ip);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid/malformed IP address: " + request.ip);
		}

		final String hostname = generateHostname(request.prefix);

		final HostnameResponseDTO response = new HostnameResponseDTO();

		response.primaryHostname = hostname;

		if (request.ssl)
		{
			final LetsEncryptCertificateEntity cert = letsEncrypt.issue(hostname);

			response.sslKeypair = new String(cert.getKeypair(), StandardCharsets.UTF_8);
			response.sslCert = new String(cert.getCert(), StandardCharsets.UTF_8);
			response.sslChain = new String(cert.getChain(), StandardCharsets.UTF_8);
		}

		if (ip instanceof Inet6Address)
			dns.createDNSRecord(hostname, RecordType.AAAA, request.ip);
		else
			dns.createDNSRecord(hostname, RecordType.A, request.ip);

		return response;
	}


	private String generateHostname(String prefix)
	{
		return prefix + "-" + SimpleId.alphanumeric(5).toUpperCase() + "." + dnsSuffix;
	}
}
