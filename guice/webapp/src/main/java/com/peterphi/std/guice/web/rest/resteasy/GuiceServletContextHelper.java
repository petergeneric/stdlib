package com.peterphi.std.guice.web.rest.resteasy;

import com.peterphi.std.guice.apploader.impl.GuiceRegistry;

import javax.servlet.ServletContext;

public final class GuiceServletContextHelper
{
	private GuiceServletContextHelper()
	{
	}


	public static GuiceRegistry get(ServletContext ctx)
	{
		GuiceRegistry reg = (GuiceRegistry) ctx.getAttribute(GuiceRegistry.class.getName());

		return reg;
	}


	public static void set(ServletContext ctx, GuiceRegistry registry)
	{
		ctx.setAttribute(GuiceRegistry.class.getName(), registry);
	}
}
