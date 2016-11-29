package com.peterphi.servicemanager.service.dns;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.microsoft.azure.management.dns.DnsManagementClient;
import com.microsoft.azure.management.dns.models.ARecord;
import com.microsoft.azure.management.dns.models.CnameRecord;
import com.microsoft.azure.management.dns.models.RecordSet;
import com.microsoft.azure.management.dns.models.RecordSetCreateOrUpdateParameters;
import com.microsoft.azure.management.dns.models.RecordSetCreateOrUpdateResponse;
import com.microsoft.azure.management.dns.models.RecordSetDeleteParameters;
import com.microsoft.azure.management.dns.models.RecordSetProperties;
import com.microsoft.azure.management.dns.models.RecordType;
import com.microsoft.azure.management.dns.models.TxtRecord;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.peterphi.std.annotation.Doc;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AzureDNS
{
	private static final Logger log = Logger.getLogger(AzureDNS.class);

	@Inject
	public Provider<DnsManagementClient> provider;

	@Inject
	@Named("azure.dns.resource-group")
	@Doc("The resource group of the Azure DNS Zones zone that will be managed")
	public String group;

	@Inject
	@Named("azure.dns.zone")
	@Doc("The Azure DNS Zones zone name that will be managed")
	public String zone;


	public boolean deleteDNSRecord(final String domainName, final RecordType recordType)
	{
		final String subdomain = getSubdomain(domainName, zone);

		try
		{
			final OperationResponse response = provider
					                                   .get()
					                                   .getRecordSetsOperations()
					                                   .delete(group,
					                                           zone,
					                                           subdomain,
					                                           recordType,
					                                           new RecordSetDeleteParameters());


			if (response.getStatusCode() < 200 || response.getStatusCode() > 299)
				throw new RuntimeException("Error code " + response.getStatusCode() + " received from Azure");
			else
				return true;
		}
		catch (Exception e)
		{
			log.warn("Error removing DNS record " + subdomain + "." + zone, e);

			return false;
		}
	}


	public static String getSubdomain(final String domainName, final String zone)
	{
		if (domainName.endsWith("." + zone))
		{
			return domainName.substring(0, domainName.length() - (zone.length() + 1));
		}
		else
		{
			throw new IllegalArgumentException("Domain " + domainName + " is not a subdomain of " + zone);
		}
	}


	public void createDNSRecord(final String domainName, final RecordType recordType, final String value)
	{
		final String subdomain = getSubdomain(domainName, zone);

		RecordSet recordset = new RecordSet("global");
		RecordSetProperties props = new RecordSetProperties();

		if (recordType == RecordType.TXT)
			props.setTxtRecords(list(new TxtRecord(value)));
		else if (recordType == RecordType.A)
			props.setARecords(list(new ARecord(value)));
		else if (recordType == RecordType.CNAME)
			props.setCnameRecord(new CnameRecord(value));
		else
			throw new IllegalArgumentException("Unsupported record type: " + recordType);

		recordset.setProperties(props);

		try
		{
			final RecordSetCreateOrUpdateResponse response = provider
					                                                 .get()
					                                                 .getRecordSetsOperations()
					                                                 .createOrUpdate(group,
					                                                                 zone,
					                                                                 subdomain,
					                                                                 recordType,
					                                                                 new RecordSetCreateOrUpdateParameters(recordset));

			if (response.getStatusCode() < 200 || response.getStatusCode() > 299)
				throw new IllegalArgumentException("Error " +
				                                   response.getStatusCode() +
				                                   " setting up DNS record " +
				                                   domainName +
				                                   "." +
				                                   zone);
		}
		catch (IOException | ServiceException e)
		{
			throw new RuntimeException("Error issuing RecordSet createOrUpdate for " + subdomain + "." + zone, e);
		}
	}


	/**
	 * Returns an ArrayList for a single item, needed because the Azure API works in ArrayList, not List(!!) so we can't just use
	 * {@link java.util.Arrays#asList(Object[])}
	 *
	 * @param item
	 * @param <T>
	 *
	 * @return
	 */
	private <T> ArrayList<T> list(T item)
	{
		return new ArrayList<T>(Collections.singletonList(item));
	}
}
