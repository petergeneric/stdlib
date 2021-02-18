package com.peterphi.std.guice.apploader;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.Arrays;
import java.util.List;

public class BasicSetup implements GuiceSetup
{
	private final Module[] modules;


	public BasicSetup()
	{
		this.modules = null;
	}


	public BasicSetup(Module... modules)
	{
		this.modules = modules;
	}


	@Override
	public void registerModules(List<Module> modules, GuiceConfig config)
	{
		if (this.modules != null)
			modules.addAll(Arrays.asList(this.modules));
	}


	@Override
	public void injectorCreated(Injector injector)
	{
	}
}
