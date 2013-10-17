package com.peterphi.carbon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.carbon.exception.CarbonConnectException;
import com.peterphi.carbon.exception.CarbonException;
import com.peterphi.carbon.message.Builder;
import com.peterphi.carbon.message.CarbonSocketAPI;
import com.peterphi.carbon.type.immutable.CarbonJobInfo;
import com.peterphi.carbon.type.immutable.CarbonProfile;
import com.peterphi.carbon.type.immutable.CarbonReply;
import com.peterphi.carbon.type.mutable.CarbonProject;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.peterphi.carbon.CarbonConfig.CARBON_HOST;
import static com.peterphi.carbon.CarbonConfig.CARBON_PORT;

@Singleton
public class CarbonClientImpl implements CarbonClient
{
	private static final Logger log = Logger.getLogger(CarbonClientImpl.class);

	private static final int MAX_CONNECT_ATTEMPTS = 3;
	private Timeout reconnectSleep = new Timeout(1, TimeUnit.SECONDS);
	private int reconnectSleepMultiplier = 2;

	private final String host;
	private final int port;

	@Inject(optional = true)
	@Named(CarbonConfig.CARBON_USER)
	private String clientId = "CarbonClientImpl_" + UUID.randomUUID().toString();

	@Inject
	private Builder builder = new Builder();

	public CarbonClientImpl(String host)
	{
		this(host, CarbonConfig.DEFAULT_CARBON_PORT);
	}

	@Inject
	public CarbonClientImpl(@Named(CARBON_HOST) String host, @Named(CARBON_PORT) Integer port)
	{
		this.host = host;
		this.port = port.intValue();
	}

	@Override
	public String getEndpoint()
	{
		return "carbon-socket://" + host + ":" + port;
	}

	@Override
	public boolean isHealthy()
	{
		try
		{
			List<CarbonProfile> profiles = getProfiles();

			if (profiles.size() > 0)
			{
				return true;
			}
			else
			{
				log.warn("Carbon " + getEndpoint() + " failed health check with no video profiles");
				return false;
			}
		}
		catch (CarbonException e)
		{
			log.warn("Carbon " + getEndpoint() + " failed health check with exception", e);
			return false;
		}
	}

	/**
	 * Send some XML
	 *
	 * @param doc
	 *
	 * @return
	 *
	 * @throws CarbonException
	 */
	public CarbonReply send(Element element) throws CarbonException
	{
		try
		{
			final String responseXml = send(serialise(element));

			return new CarbonReply(deserialise(responseXml));
		}
		catch (CarbonException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new CarbonException(e);
		}
	}

	private String send(byte[] data) throws CarbonException
	{
		try
		{
			log.trace("Connecting to Carbon...");

			final Socket socket = createSocket();

			final CarbonSocketAPI api = new CarbonSocketAPI(socket);

			return api.send(data);
		}
		catch (CarbonException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new CarbonException(e);
		}
	}

	private Socket createSocket()
	{
		int attempt = 0;
		Timeout sleep = reconnectSleep;

		while (attempt++ < MAX_CONNECT_ATTEMPTS)
			try
			{
				return new Socket(host, port);
			}
			catch (Exception e)
			{
				if (attempt < MAX_CONNECT_ATTEMPTS)
				{
					sleep.sleep(); // back off

					// Sleep longer next time
					sleep = sleep.multiply(reconnectSleepMultiplier);
				}
				else
				{
					throw new CarbonConnectException("Could not connect to Carbon API in " + MAX_CONNECT_ATTEMPTS + " attempts",
					                                 e);
				}
			}

		throw new CarbonConnectException("Could not conect to Carbon API socket!");
	}

	private static byte[] serialise(Element element)
	{
		try
		{
			final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

			new XMLOutputter().output(element, bos);

			return bos.toByteArray();
		}
		catch (IOException e)
		{
			throw new CarbonException(e.getMessage(), e);
		}
	}

	private static Document deserialise(String xml)
	{
		try
		{
			return new SAXBuilder().build(new StringReader(xml));
		}
		catch (IOException e)
		{
			throw new CarbonException(e.getMessage(), e);
		}
		catch (JDOMException e)
		{
			throw new CarbonException(e.getMessage(), e);
		}
	}

	@Override
	public CarbonJobInfo getJob(String guid) throws CarbonException
	{
		CarbonReply reply = send(new Builder().createJobInfoRequest(guid));

		if (!reply.isSuccess())
			throw new CarbonException("Failure acquiring info for job " + guid);
		else
			return reply.getJobInfo();
	}

	public CarbonProject getJobDetails(String guid) throws CarbonException
	{
		CarbonReply reply = send(new Builder().createJobFullInfoRequest(guid));

		if (!reply.isSuccess())
		{
			throw new CarbonException("Failure acquiring info for job " + guid);
		}
		else
		{
			return reply.getJob();
		}
	}

	@Override
	public String createJob(CarbonProject project) throws CarbonException
	{
		// Set the user if it's not already specified
		if (project.getUser() == null)
			project.setUser(clientId);

		CarbonReply reply = send(project.getElement());

		if (!reply.isSuccess())
			throw new CarbonException("Carbon createJob call failed. Error returned was: " + reply.getError());
		else
			return reply.getGUID();
	}

	public List<String> listJobs() throws CarbonException
	{
		CarbonReply reply = send(builder.createJobListRequest());

		if (!reply.isSuccess())
			throw new CarbonException("Carbon call failed. Error returned was: " + reply.getError());
		else
			return reply.getJobIdList();
	}

	@Override
	public List<CarbonProfile> getProfiles() throws CarbonException
	{
		final CarbonReply reply = send(builder.createDestinationProfileListRequest());

		if (!reply.isSuccess())
			throw new CarbonException("Carbon call failed. Error returned was: " + reply.getError());
		else
			return reply.getProfileList();
	}

	@Override
	public CarbonProfile getProfile(String query) throws CarbonException
	{
		for (CarbonProfile profile : getProfiles())
		{
			final String name = profile.getName();
			final String guid = profile.getGUID();
			if (query.equalsIgnoreCase(name) || query.equalsIgnoreCase(guid))
				return profile;
		}

		return null;
	}

	@Override
	public CarbonProfile getVideoFilter(String query)
	{
		for (CarbonProfile profile : getVideoFilters())
		{
			final String name = profile.getName();
			final String guid = profile.getGUID();
			if (query.equalsIgnoreCase(name) || query.equalsIgnoreCase(guid))
				return profile;
		}

		return null;
	}

	@Override
	public List<CarbonProfile> getVideoFilters() throws CarbonException
	{
		final CarbonReply reply = send(builder.createVideoFilterProfileListRequest());

		if (!reply.isSuccess())
			throw new CarbonException("Carbon call failed. Error returned was: " + reply.getError());
		else
			return reply.getProfileList();
	}

	@Override
	public void removeJob(String guid) throws CarbonException
	{
		final CarbonReply reply = send(builder.createJobCommandRequest(guid, "Remove"));

		if (!reply.isSuccess())
			throw new CarbonException("Carbon call failed. Error returned was: " + reply.getError());
	}
}
