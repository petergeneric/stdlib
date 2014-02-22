package com.peterphi.std.types;

import com.peterphi.std.net.IpHelper;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;

/**
 * Represents an IPv4 network subnet (a combination of an IP address and a prefix. For example, 193.61.123.0/24 represents
 * 193.61.123.1 to 193.61.123.254)<br />
 * The data stored by this class is similar to the InterfaceAddress class, however that class is non-constructable and does not
 * include helper methods to determine subnet membership
 */
public class InetSubnet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The entire IPv4 address space (including restricted/reserved/private IPs)<br />
	 * This is the subnet 0.0.0.0/0
	 */
	public static final InetSubnet IPV4_ADDRESS_SPACE = new InetSubnet(0, 0); // "0.0.0.0/0"

	public final InetAddress network;
	public final int prefix;

	// The following fields are populated by recache()
	private transient int maskedNetwork;
	private transient int mask;


	/*
	 * Copies the subnet as specified by an InterfaceAddress
	 * 
	 * @param addr
	 */
	public InetSubnet(final InterfaceAddress addr)
	{
		this.network = addr.getAddress();
		this.prefix = addr.getNetworkPrefixLength();
	}


	/**
	 * Constructs an inet subnet from CIDR representation (<code>x.x.x.x/prefix</code>)
	 *
	 * @param tostring
	 */
	public InetSubnet(final String cidr)
	{
		final int i = cidr.indexOf('/');

		if (i != -1)
		{
			try
			{
				this.network = InetAddress.getByName(cidr.substring(0, i));
				this.prefix = Integer.parseInt(cidr.substring(i + 1));

				recache();
			}
			catch (UnknownHostException e)
			{
				throw new IllegalArgumentException("for InetSubnet(string) the left segment string must be a valid IP address. Illegal input was: " +
				                                   cidr, e);
			}
		}
		else
		{
			throw new IllegalArgumentException("InetSubnet(string) must have a / in it. Illegal input was: " + cidr);
		}
	}


	/**
	 * @param ip
	 * @param prefix
	 *
	 * @throws IllegalArgumentException
	 * 		if the IP address cannot be parsed
	 */
	public InetSubnet(String ip, int prefix)
	{
		try
		{
			this.network = InetAddress.getByName(ip);
			this.prefix = prefix;

			recache();
		}
		catch (UnknownHostException e)
		{
			throw new IllegalArgumentException("for InetSubnet(string,int) the string must be a valid IP address. Illegal input was: " +
			                                   ip, e);
		}
	}


	/**
	 * Construct a subnet based on
	 *
	 * @param ip
	 * @param prefix
	 */
	public InetSubnet(int ip, int prefix)
	{
		this.network = IpHelper.ntoa(ip);
		this.prefix = prefix;
	}


	/**
	 * Produces a subnet based on a network IP and a prefix
	 *
	 * @param network
	 * @param prefix
	 */
	public InetSubnet(final InetAddress network, final int prefix)
	{
		this.network = network;
		this.prefix = prefix;

		recache();
	}


	/**
	 * Produces a subnet based on a network IP and a netmask. The internal representation always uses the prefix style
	 *
	 * @param network
	 * @param netmask
	 */
	public InetSubnet(final InetAddress network, final InetAddress netmask)
	{
		this.network = network;
		this.prefix = IpHelper.netmaskToPrefix(netmask);

		recache();
	}


	/**
	 * Repopulates the transient fields based on the IP and prefix
	 */
	private void recache()
	{
		// If we've already cached the values then don't bother recalculating; we
		// assume a mask of 0 means a recompute is needed (unless prefix is also 0)
		// We skip the computation completely is prefix is 0 - this is fine, since
		// the mask and maskedNetwork for prefix 0 result in 0, the default values.

		// We need to special-case /0 because our mask generation code doesn't work for
		// prefix=0 (since -1 << 32 != 0)
		if (mask == 0 && prefix != 0)
		{
			this.mask = -1 << (32 - prefix);
			this.maskedNetwork = IpHelper.aton(network) & mask;
		}
	}


	/**
	 * Emits this subnet in CIDR notation using the prefix (eg. x.x.x.x/prefix)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		recache();

		return network.getHostAddress() + "/" + prefix;
	}


	@Override
	public int hashCode()
	{
		return this.network.hashCode() ^ this.prefix;
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o.getClass().equals(InetSubnet.class))
		{
			InetSubnet that = (InetSubnet) o;

			return this.network.equals(that.network) && this.prefix == that.prefix;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Returns the IP address represented by this subnet; when in its canonical form, <code>getIP()</code> will return the same as
	 * <code>getNetwork()</code>
	 *
	 * @return The IP address represented by this subnet; this may be different from the network InetAddress when the InetSubnet
	 * isn't canonicalised
	 */
	public InetAddress getIP()
	{
		return this.network;
	}


	/**
	 * Returns the InetAddress representing the network (fully masked by the prefix); this is the IP address with the last
	 * <code>prefix</code> bits masked to zero
	 *
	 * @return
	 */
	public InetAddress getNetwork()
	{
		recache();

		return IpHelper.ntoa(maskedNetwork);
	}


	/**
	 * Returns the prefix length (0 to 32 for an IPv4 subnet)<br />
	 * The larger the prefix the larger the block if IP addresses represented by this subnet
	 *
	 * @return
	 */
	public int getPrefix()
	{
		return prefix;
	}


	/**
	 * Retrieves the prefix as a netmask (eg. a prefix of 24 becomes 255.255.255.0)
	 *
	 * @return
	 */
	public InetAddress getNetmask()
	{
		recache();

		return IpHelper.ntoa(this.mask);
	}


	/**
	 * Produces a canonical representation of this subnet; the canonical form has the network ip address properly masked (eg.
	 * <code>10.0.0.3/31</code> and <code>10.0.0.2/31</code> both canonicalise to <code>10.0.0.2/31</code>)
	 *
	 * @return
	 */
	public InetSubnet canonicalise()
	{
		// We don't need to call recache() because isCanonical calls it for us
		if (!isCanonical())
		{
			return new InetSubnet(maskedNetwork, prefix);
		}
		else
		{
			return this;
		}
	}


	/**
	 * Determines whether this InetSubnet is currently in its canonical form
	 *
	 * @return
	 */
	public boolean isCanonical()
	{
		recache();

		return IpHelper.aton(network) == this.maskedNetwork;
	}


	/**
	 * Determines whether a given IP, in numeric representation (as returned by <code>aton()</code>) is a member of this subnet<br
	 * />
	 * This is the fastest way to determine subnet membership
	 *
	 * @param ip
	 *
	 * @return
	 */
	public boolean isMember(final int ip)
	{
		recache();

		return (ip & mask) == maskedNetwork;
	}


	/**
	 * Determines whether a given IP, given as an IPv4 address in its string representation (x.x.x.x) is a member of this subnet
	 *
	 * @param ip
	 *
	 * @return
	 *
	 * @throws IllegalArgumentException
	 * 		if the IP cannot be parsed
	 */
	public boolean isMember(final String ip)
	{
		return isMember(IpHelper.aton(ip));
	}


	/**
	 * Determines whether a given IP is a member of this subnet
	 *
	 * @param ip
	 *
	 * @return
	 */
	public boolean isMember(final InetAddress ip)
	{
		return isMember(IpHelper.aton(ip));
	}


	/**
	 * @param ip
	 *
	 * @return
	 *
	 * @deprecated use isMember instead
	 */
	@Deprecated
	public boolean member(int ip)
	{
		return isMember(ip);
	}


	/**
	 * @param ip
	 *
	 * @return
	 *
	 * @deprecated use isMember instead
	 */
	@Deprecated
	public boolean member(String ip)
	{
		return isMember(ip);
	}


	/**
	 * @param ip
	 *
	 * @return
	 *
	 * @deprecated use isMember instead
	 */
	@Deprecated
	public boolean member(InetAddress ip)
	{
		return isMember(ip);
	}


	/**
	 * Returns the number of hosts in this subnet
	 *
	 * @return
	 */
	public int getHosts()
	{
		return (int) Math.pow(2, (32 - this.prefix)) - 2;
	}


	/**
	 * Gets the first host in this subnet
	 *
	 * @return
	 */
	public InetAddress getHostMin()
	{
		recache();

		return IpHelper.ntoa(this.maskedNetwork + 1);
	}


	/**
	 * Gets the last host in this subnet
	 *
	 * @return
	 */
	public InetAddress getHostMax()
	{
		recache();

		return IpHelper.ntoa(this.maskedNetwork + getHosts());
	}


	/**
	 * Returns the broadcast address of this subnet
	 *
	 * @return
	 */
	public InetAddress getBroadcast()
	{
		recache();

		return IpHelper.ntoa(this.maskedNetwork + this.getHosts() + 1);
	}


	/**
	 * Retrieves the wildcard mask for the netmask of this subnet
	 *
	 * @return the inversion of the netmask (eg. 255.255.255.0 becomes 0.0.0.255)
	 */
	public InetAddress getWildcardMask()
	{
		recache();

		return IpHelper.ntoa(~this.mask);
	}

}
