package com.peterphi.std.guice.web.rest.templating.thymeleaf;

import com.peterphi.std.guice.common.cached.util.DummyCache;
import org.thymeleaf.TemplateEngine;

import java.util.function.Supplier;

final class ThymeleafCacheEmptyHook extends DummyCache
{
	private final Supplier<TemplateEngine> engine;


	public ThymeleafCacheEmptyHook(final Supplier<TemplateEngine> engine)
	{
		this.engine = engine;
	}


	@Override
	public void invalidateAll()
	{
		final TemplateEngine engine = this.engine.get();

		if (engine != null)
			engine.clearTemplateCache();
	}


	@Override
	public long size()
	{
		final TemplateEngine engine = this.engine.get();

		if (engine != null)
			return engine.getCacheManager().getTemplateCache().keySet().size();
		else
			return -1;
	}
}
