package com.peterphi.carbon;

import com.google.inject.ImplementedBy;
import com.peterphi.carbon.exception.CarbonException;
import com.peterphi.carbon.type.immutable.CarbonJobInfo;
import com.peterphi.carbon.type.immutable.CarbonProfile;
import com.peterphi.carbon.type.immutable.CarbonReply;
import com.peterphi.carbon.type.mutable.CarbonProject;
import org.jdom2.Element;

import java.util.List;

@ImplementedBy(CarbonClientImpl.class)
public interface CarbonClient
{
	public String getEndpoint();

	/**
	 * Perform a health check on the Carbon instance
	 *
	 * @return true if healthy, otherwise false
	 */
	public boolean isHealthy();

	/**
	 * Send an arbitrary XML request
	 *
	 * @param element
	 * 		the XML element
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public CarbonReply send(Element element) throws CarbonException;

	public CarbonJobInfo getJob(String guid) throws CarbonException;

	/**
	 * Get detailed information about a job
	 *
	 * @param guid
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public CarbonProject getJobDetails(String guid) throws CarbonException;

	public String createJob(CarbonProject project) throws CarbonException;

	/**
	 * @return
	 *
	 * @throws CarbonException
	 */
	public List<String> listJobs() throws CarbonException;

	/**
	 * Retrieve a list of all destination profiles
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public List<CarbonProfile> getProfiles() throws CarbonException;

	public List<CarbonProfile> getVideoFilters() throws CarbonException;

	/**
	 * Retrieve a profile by name (or GUID)
	 *
	 * @param name
	 * 		the profile name (or GUID)
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public CarbonProfile getProfile(String name) throws CarbonException;

	public void removeJob(String guid) throws CarbonException;

	/**
	 * Retrieve a video filter by name (or GUID)
	 *
	 * @param name
	 * 		the video filter name (or GUID)
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public CarbonProfile getVideoFilter(String name);
}
