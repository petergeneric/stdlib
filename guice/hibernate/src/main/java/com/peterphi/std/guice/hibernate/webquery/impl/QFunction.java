package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;

public interface QFunction
{
	HQLFragment encode(HQLEncodingContext ctx);
}
