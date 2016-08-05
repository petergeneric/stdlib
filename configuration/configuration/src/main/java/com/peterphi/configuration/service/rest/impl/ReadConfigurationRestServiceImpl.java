package com.peterphi.configuration.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigRepository;
import com.peterphi.std.guice.config.rest.iface.ConfigRestService;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;

public class ReadConfigurationRestServiceImpl implements ConfigRestService
{
	@Inject
	@Named("config")
	public ConfigRepository repo;


	@Override
	public ConfigPropertyData read(final String path, final String instanceId, final String lastRevision)
	{
		return repo.get("HEAD", path);
	}
}
