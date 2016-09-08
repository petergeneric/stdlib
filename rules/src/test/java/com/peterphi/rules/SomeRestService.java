package com.peterphi.rules;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by bmcleod on 08/09/2016.
 */
@Path("/foo")
public interface SomeRestService
{
	@GET
	public String get();
}
