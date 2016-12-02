package com.peterphi.servicemanager.service.rest.resource.helper;

import com.peterphi.servicemanager.service.rest.resource.iface.ServiceManagerResourceRestService;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQFunctionType;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.List;

/**
 * Created by bmcleod on 18/11/2016.
 */
public class ResourceInstanceHelper
{
	/**
	 * Returns the number of active instances of the given template name
	 *
	 * @param service
	 * @param templateName
	 *
	 * @return
	 */
	public int countActiveInstances(ServiceManagerResourceRestService service, String templateName)
	{
		List<ResourceInstanceDTO> instances = activeInstances(service, templateName);

		return instances.size();
	}


	/**
	 * Returns the number of active instances of the given template name
	 *
	 * @param service
	 * @param templateName
	 *
	 * @return
	 */
	public List<ResourceInstanceDTO> activeInstances(ServiceManagerResourceRestService service, String templateName)
	{
		WebQuery wq = new WebQuery();
		wq.eq("template.id", templateName);
		wq.eq("state",
		      ResourceInstanceState.TO_PROVISION,
		      ResourceInstanceState.PROVISIONING,
		      ResourceInstanceState.NOT_IN_SERVICE,
		      ResourceInstanceState.IN_SERVICE);

		List<ResourceInstanceDTO> instances = service.searchInstances(wq);

		return instances;
	}


	public void stopActiveInstances(ServiceManagerResourceRestService service, String templateName)
	{
		for (ResourceInstanceDTO resourceInstanceDTO : activeInstances(service, templateName))
		{
			service.discardInstance(resourceInstanceDTO.id);
		}
		;
	}
}
