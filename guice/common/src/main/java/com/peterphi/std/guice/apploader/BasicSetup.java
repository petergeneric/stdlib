package com.peterphi.std.guice.apploader;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicSetup implements GuiceSetup
{
	private final List<Module> modules;


	public BasicSetup(List<Module> modules)
	{
		this.modules = modules;
	}


	public BasicSetup(Module... modules)
	{
		this.modules = new ArrayList<Module>();

		if (modules != null)
			Collections.addAll(this.modules, modules);
	}


	@Override
	public void registerModules(List<Module> modules, Configuration config)
	{
		modules.addAll(this.modules);
	}


	@Override
	public void injectorCreated(Injector injector)
	{
	}
}
