package com.peterphi.std.guice.common.resource.iface;

import com.peterphi.std.io.PropertyFile;

import java.io.InputStream;
import java.io.Reader;

/**
 * Interface to a provider of resources.<br />
 * Paths are specified in the style of a UNIX filepath, using "/" as a separator character.
 */
public interface ResourceProvider
{
	/**
	 * Loads a binary resource
	 *
	 * @param name
	 * 		the resource name, expressed as a path (see the comments on ResourceProvider)
	 *
	 * @return
	 *
	 * @throws ResourceNotFoundException
	 */
	public InputStream getBinaryResource(String name) throws ResourceNotFoundException;

	/**
	 * Loads a text resource. If it is necessary to make assumptions about the underlying encoding of a resource (e.g. the remote
	 * resource provider does not support then UTF-8 will be used
	 *
	 * @param name
	 * 		the resource name, expressed as a path using / as a separator character
	 *
	 * @return
	 *
	 * @throws ResourceNotFoundException
	 */
	public Reader getTextResource(String name) throws ResourceNotFoundException;

	/**
	 * Loads a resource containing a property file
	 *
	 * @param name
	 * 		the resource name, expressed as a path with / characters to separate
	 *
	 * @return
	 *
	 * @throws ResourceNotFoundException
	 */
	public PropertyFile getPropertyResource(String name) throws ResourceNotFoundException;
}
