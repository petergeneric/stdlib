package com.peterphi.std.net;

import org.apache.log4j.Logger;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/*
 * <p> Title: Ip Helper </p>
 * 
 * <p> Description: IP address-related helper functions </p>
 * 
 * <p> Copyright: Copyright (c) 2006-2009 </p>
 * 
 * <p>  </p>
 * 
 * 
 * @version $Revision$
 */
public class IpHelper
{
	private static final Logger log = Logger.getLogger(IpHelper.class);


	/**
	 * The "Quad Zero" IPv4 address: <code>0.0.0.0</code>
	 */
	public static final Inet4Address QUAD_ZERO = (Inet4Address) IpHelper.ntoa(0);

	/**
	 * The IPv4 broadcast address: <code>255.255.255.255</code>
	 */
	public static final Inet4Address BROADCAST_ADDRESS = (Inet4Address) IpHelper.ntoa(-1);

	/**
	 * The IPv6 equivalent of the "Quad Zero" IPv4 address (0.0.0.0). This is the Inet6Address <code>::</code>
	 */
	public static final Inet6Address IPV6_ZERO = (Inet6Address) IpHelper.stoa("::");


	// Prevent instantiation
	private IpHelper()
	{
	}


	/**
	 * Gets the hostname of localhost
	 *
	 * @return String
	 *
	 * @throws RuntimeException
	 * 		When we cannot determine the local machine's hostname
	 */
	public static String getLocalhost() throws RuntimeException
	{
		String hostname = null;

		try
		{
			InetAddress addr = InetAddress.getLocalHost();

			hostname = addr.getHostName();
		}
		catch (UnknownHostException e)
		{
			throw new RuntimeException("[FileHelper] {getLocalhost}: Can't get local hostname");
		}

		return hostname;
	}


	/**
	 * Gets the local IP address
	 *
	 * @return String the local ip address's HostAddress
	 *
	 * @throws RuntimeException
	 * 		On error (eg. when the local host cannot be determined)
	 */
	public static String getLocalIp() throws RuntimeException
	{
		try
		{
			InetAddress addr = getLocalIpAddress();

			return addr.getHostAddress();
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException("[FileHelper] {getLocalIp}: Unable to find the local machine", e);
		}
	}


	/**
	 * Returns the primary InetAddress of localhost
	 *
	 * @return InetAddress The InetAddress of the local host
	 *
	 * @throws RuntimeException
	 * 		On any error
	 * @since 1.0
	 */
	public static InetAddress getLocalIpAddress() throws RuntimeException
	{
		try
		{
			List<InetAddress> ips = getLocalIpAddresses(false, true);

			for (InetAddress ip : ips)
			{
				log.debug("[IpHelper] {getLocalIpAddress} Considering locality of " + ip.getHostAddress());
				if (!ip.isAnyLocalAddress() && (ip instanceof Inet4Address))
				{
					// Ubuntu sets the unix hostname to resolve to 127.0.1.1; this is annoying so treat it as a loopback
					if (!ip.getHostAddress().startsWith("127.0."))
					{
						log.debug("[IpHelper] {getLocalIpAddress} Found nonloopback IP: " + ip.getHostAddress());
						return ip;
					}
				}
			}

			log.trace("[IpHelper] {getLocalIpAddress} Couldn't find a public IP in the ip (size " + ips.size() + ")");
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
			throw new RuntimeException("[FileHelper] {getLocalIp}: Unable to acquire the current machine's InetAddress", e);
		}
	}


	/**
	 * Returns the IP address associated with iface
	 *
	 * @param iface
	 * 		The interface name
	 *
	 * @return InetAddress The InetAddress of the interface (or null if none found)
	 *
	 * @throws RuntimeException
	 * 		On any error
	 * @since 1.0
	 */
	public static InetAddress getLocalIpAddress(String iface) throws RuntimeException
	{
		try
		{
			NetworkInterface nic = NetworkInterface.getByName(iface);

			Enumeration<InetAddress> ips = nic.getInetAddresses();

			InetAddress firstIP = null;
			while (ips != null && ips.hasMoreElements())
			{
				InetAddress ip = ips.nextElement();
				if (firstIP == null)
					firstIP = ip;

				if (log.isDebugEnabled())
					log.debug("[IpHelper] {getLocalIpAddress} Considering locality: " + ip.getHostAddress());

				if (!ip.isAnyLocalAddress())
				{
					return ip;
				}
			}

			// Return the first IP (or null if no IPs were returned)
			return firstIP;
		}
		catch (SocketException e)
		{
			throw new RuntimeException("[IpHelper] {getLocalIpAddress}: Unable to acquire an IP", e);
		}
	}


	/**
	 * Returns a list of local InetAddresses for this machine
	 *
	 * @return List[InetAddress] The list of IP addresses
	 *
	 * @throws RuntimeException
	 * 		If there is an error retrieving the InetAddresses
	 * @since 1.2
	 */
	public static List<InetAddress> getLocalIpAddresses() throws RuntimeException
	{
		return getLocalIpAddresses(false, false);
	}


	/**
	 * Returns a list of local InetAddresses for this machine
	 *
	 * @param pruneSiteLocal
	 * 		boolean Set to true if site local (10/8, for example) addresses should be pruned
	 *
	 * @return List[InetAddress] The list of addresses
	 *
	 * @throws RuntimeException
	 * 		If there is an error retrieving the InetAddresses
	 * @since IpHelper.java 1.2
	 */
	public static List<InetAddress> getLocalIpAddresses(boolean pruneSiteLocal) throws RuntimeException
	{
		return getLocalIpAddresses(pruneSiteLocal, false);
	}


	/**
	 * Returns a list of local InetAddresses for this machine
	 *
	 * @param pruneSiteLocal
	 * 		boolean Set to true if site local (10/8, for example) addresses should be pruned
	 * @param pruneDown
	 * 		boolean Set to true to prune out interfaces whose .isUp() return false
	 *
	 * @return List[InetAddress] The list of addresses
	 *
	 * @throws RuntimeException
	 * 		If there is an error retrieving the InetAddresses
	 * @since IpHelper.java 2007-11-22
	 */
	public static List<InetAddress> getLocalIpAddresses(boolean pruneSiteLocal, boolean pruneDown) throws RuntimeException
	{
		try
		{
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			List<InetAddress> addresses = new Vector<InetAddress>();

			while (nics.hasMoreElements())
			{
				NetworkInterface iface = nics.nextElement();
				Enumeration<InetAddress> addrs = iface.getInetAddresses();

				if (!pruneDown || iface.isUp())
				{
					while (addrs.hasMoreElements())
					{
						InetAddress addr = addrs.nextElement();

						if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress())
						{
							if (!pruneSiteLocal || (pruneSiteLocal && !addr.isSiteLocalAddress()))
							{
								addresses.add(addr);
							}
						}
					}
				}
			}

			return addresses;
		}
		catch (SocketException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * Given an InetAddress, determines the MAC Address (hardware address) of the corresponding interface
	 *
	 * @param addr
	 * 		The IP address
	 *
	 * @return The MAC Address
	 *
	 * @throws SocketException
	 * 		If we couldn't get the interface
	 * @throws NoMacAddressException
	 * 		If the interface doesn't have a mac address specified
	 */
	public static String getMacForLocalIp(InetAddress addr) throws SocketException, NoMacAddressException, NoInterfaceException
	{
		return getMacFor(getInterfaceForLocalIp(addr));
	}


	/**
	 * Given a network interface, determines its mac address
	 *
	 * @param iface
	 * 		the interface
	 *
	 * @return The MAC address formatted in lower-case colon-delimited hexidecimal bytes (aa:ab:ac)
	 *
	 * @throws SocketException
	 * @throws NoMacAddressException
	 * 		If the interface doesn't have a mac address
	 */
	public static String getMacFor(NetworkInterface iface) throws SocketException, NoMacAddressException
	{
		assert (iface != null);

		byte[] hwaddr = iface.getHardwareAddress();

		if (hwaddr == null || hwaddr.length == 0)
		{
			throw new NoMacAddressException("Interface " + iface.getName() + " has no physical address specified.");
		}
		else
		{
			return physicalAddressToString(hwaddr);
		}
	}


	/**
	 * Given a local IP address, returns the Interface it corresponds to
	 *
	 * @param addr
	 * 		The address
	 *
	 * @return The interface, or null if no interface corresponds to that address
	 *
	 * @throws SocketException
	 * @throws NoInterfaceException
	 * 		If there's no corresponding interface for the given IP
	 */
	public static NetworkInterface getInterfaceForLocalIp(InetAddress addr) throws SocketException, NoInterfaceException
	{
		assert (getLocalIpAddresses(false).contains(addr)) : "IP is not local";

		NetworkInterface iface = NetworkInterface.getByInetAddress(addr);

		if (iface != null)
			return iface;
		else
			throw new NoInterfaceException("No network interface for IP: " + addr.toString());
	}

	private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


	public static String physicalAddressToString(byte[] addr)
	{
		StringBuffer sb = new StringBuffer((addr.length * 3) - 1);

		for (int i = 0;/* termination handled by final if */ ; ++i)
		{
			byte lo = (byte) (addr[i] & 0x0F);
			byte hi = (byte) ((addr[i] & 0xF0) >>> 4);

			sb.append(hex[hi]);
			sb.append(hex[lo]);

			if (i + 1 < addr.length)
			{
				sb.append(":");
			}
			else
			{
				break;
			}
		}

		return sb.toString();
	}


	public static List<InetAddress> getLocalIpAddresses(String ifaceName, boolean pruneSiteLocal) throws RuntimeException
	{
		try
		{
			NetworkInterface iface = NetworkInterface.getByName(ifaceName);

			List<InetAddress> addresses = new Vector<InetAddress>();
			Enumeration<InetAddress> addrs = iface.getInetAddresses();

			while (addrs.hasMoreElements())
			{
				InetAddress addr = addrs.nextElement();

				if (!addr.isLoopbackAddress() || (pruneSiteLocal && !addr.isLinkLocalAddress()))
				{
					addresses.add(addr);
				}
			}

			return addresses;
		}
		catch (SocketException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * Validates a netmask
	 *
	 * @param netmask
	 * 		String A netmask to test
	 *
	 * @return boolean True if it validates, otherwise false
	 */
	public static boolean isValidNetmask(String netmask)
	{
		try
		{
			return isValidNetmask(InetAddress.getByName(netmask));
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}


	/**
	 * Validates a netmask
	 *
	 * @param netmask
	 * 		InetAddress A netmask to test
	 *
	 * @return boolean True if it validates, otherwise false
	 */
	public static boolean isValidNetmask(InetAddress netmask)
	{

		byte[] segments = netmask.getAddress();

		return isValidNetmask(segments);
	}


	/**
	 * Validates a netmask
	 *
	 * @param segments
	 * 		byte[] A signed byte array whose binary values represent the address
	 *
	 * @return boolean True if valid, otherwise false
	 */
	public static boolean isValidNetmask(byte[] segments)
	{
		boolean mustBeZero = false;
		for (int i = 0; i < segments.length; i++)
		{

			// Valid segments: 0, 128, 192, 224, 240, 248, 252, 254, 255
			switch (segments[i])
			{
				case -1:
					if (mustBeZero)
						return false;
					break;
				case 0:
					if (mustBeZero)
					{
						break;
					}
				case -2:
				case -4:
				case -8:
				case -16:
				case -32:
				case -64:
				case -128:
					if (!mustBeZero)
						mustBeZero = true;
					else
					{
						return false;
					}
					break;
				default:
					return false;
			}
		}

		return true;
	}


	/**
	 * Determines the prefix for a given netmask (ie. turns 255.255.255.0 into 24)<br />
	 * Returns the number of contiguous 1 bits. Throws an IllegalArgumentException if the address is an invalid netmask
	 *
	 * @param netmask
	 * 		An IP netmask (eg. 255.255.252.0)
	 *
	 * @return A prefix length (the number of 1-bits in the netmask)
	 *
	 * @throws IllegalArgumentException
	 * 		if an invalid netmask is passed
	 */
	public static int netmaskToPrefix(InetAddress netmask)
	{
		byte[] mask = netmask.getAddress();

		if (!isValidNetmask(mask))
			throw new IllegalArgumentException("Not a valid netmask: " + netmask.getHostAddress());

		int prefix = 0;
		for (int i = 0; i < mask.length; i++)
		{
			// drops-through all lower cases to accumulate additions (so case: -2 becomes prefix += 7)
			switch (mask[i])
			{
				case -1:
					prefix += 8; // Hand-optimisation for a 255 segment (since it's so frequent)
					break;
				case -2:
					prefix++;
				case -4:
					prefix++;
				case -8:
					prefix++;
				case -16:
					prefix++;
				case -32:
					prefix++;
				case -64:
					prefix++;
				case -128:
					prefix++;
				default:
			}
		}

		return prefix;
	}


	/**
	 * Turns an IPv4 prefix into a netmask
	 *
	 * @param prefix
	 *
	 * @return
	 */
	public static InetAddress prefixToNetmask(final int prefix)
	{
		return IpHelper.ntoa(prefixToMask(prefix));
	}


	/**
	 * Turns an IPv4 prefix into a numeric mask (the equivalent of running <code>IpHelper.aton(prefixToNetmask(prefix))</code> but
	 * significantly faster)
	 *
	 * @param prefix
	 *
	 * @return
	 */
	public static int prefixToMask(final int prefix)
	{
		// We need to special-case zero because -1 << 32 != 0
		if (prefix != 0)
			return -1 << (32 - prefix);
		else
			return 0;
	}


	/**
	 * Determines if a specified address is a valid Cisco Wildcard (cisco's representation of a netmask)
	 *
	 * @param wildcard
	 * 		InetAddress
	 *
	 * @return boolean
	 */
	public static boolean isValidCiscoWildcard(InetAddress wildcard)
	{
		byte[] segments = wildcard.getAddress();

		for (int i = 0; i < segments.length; i++)
		{
			assert (((byte) ~(byte) ~segments[i]) == segments[i]);
			segments[i] = (byte) ~segments[i];
		}

		return isValidNetmask(segments);
	}


	/**
	 * Determines if a specified host or IP refers to the local machine
	 *
	 * @param addr
	 * 		String The host/IP
	 *
	 * @return boolean True if the input points to the local machine, otherwise false
	 */
	public static boolean isLocalAddress(String addr)
	{
		try
		{
			InetAddress iAddr = InetAddress.getByName(addr);
			return isLocalAddress(iAddr);
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}


	/**
	 * Determines if a specified host or IP refers to the local machine
	 *
	 * @param addr
	 * 		String The host/IP
	 *
	 * @return boolean True if the input points to the local machine, otherwise false Checks by enumerating the NetworkInterfaces
	 * available to Java.
	 */
	public static boolean isLocalAddress(final InetAddress addr)
	{
		if (addr.isLoopbackAddress())
		{
			return true;
		}

		try
		{
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();

			while (nics.hasMoreElements())
			{
				Enumeration<InetAddress> addrs = nics.nextElement().getInetAddresses();

				while (addrs.hasMoreElements())
				{
					if (addrs.nextElement().equals(addr))
					{
						return true; // Search successful
					}
				}
			}
		}
		catch (SocketException e)
		{
			log.debug(e.getMessage(), e);
		}

		log.debug("[FileHelper] {isLocalAddress} not local: " + addr.getHostAddress());

		// Search failed
		return false;
	}


	/**
	 * Parses an IP address, returning it as an InetAddress<br />
	 * This method exists to prevent code which is handling valid IP addresses from having to catch UnknownHostException
	 * excessively (when there is often no choice but to totally fail out anyway)
	 *
	 * @param ip
	 * 		a valid IP address (IPv4 or IPv6)
	 *
	 * @return the resulting InetAddress for that ip; this is equivalent to calling <code>InetAddress.getByName</code> on the IP
	 * (but without having to catch UnknownHostException)
	 *
	 * @throws IllegalArgumentException
	 * 		if the IP address is invalid (eg. null, an empty string or otherwise not in the valid IP address format)
	 */
	public static InetAddress stoa(final String ip)
	{
		if (ip == null || ip.isEmpty())
			throw new IllegalArgumentException("must pass a valid ip: null or empty strings are not valid IPs!");

		try
		{
			return InetAddress.getByName(ip);
		}
		catch (UnknownHostException e)
		{
			throw new IllegalArgumentException("must pass a valid ip. Illegal input was: " + ip, e);
		}
	}


	/**
	 * An equivalent of the C <code>inet_aton</code> function
	 *
	 * @param ip
	 *
	 * @return
	 */
	public static int aton(final String ip)
	{
		try
		{
			return aton(InetAddress.getByName(ip));
		}
		catch (UnknownHostException e)
		{
			throw new IllegalArgumentException("must pass a valid ip. Illegal input was: " + ip, e);
		}
	}


	/**
	 * Converts an InetAddress to a numeric address <strong>only</strong> in the case of IPv4 (Inet4Address) addresses. Other
	 * addresses will result in an Error being thrown
	 *
	 * @param ip
	 *
	 * @return
	 */
	public static int aton(final InetAddress ip)
	{
		if (ip == null)
		{
			throw new Error("the result of aton(null) is undefined");
		}
		if (ip instanceof Inet4Address)
		{
			return aton((Inet4Address) ip);
		}
		else
		{
			throw new Error("int aton(InetAddress) does not function for " + ip.getClass());
		}
	}


	/**
	 * Converts an InetAddress to a numeric address
	 *
	 * @param ip
	 *
	 * @return
	 */
	public static int aton(final Inet4Address ip)
	{
		return aton(ip.getAddress());
	}


	/**
	 * Converts an InetAddress' byte[] representation to a numeric address<br />
	 * This only works for IPv4 (obviously)
	 *
	 * @param addr
	 *
	 * @return
	 */
	public static int aton(final byte[] addr)
	{
		int address = addr[3] & 0xFF;
		address |= ((addr[2] << 8) & 0xFF00);
		address |= ((addr[1] << 16) & 0xFF0000);
		address |= ((addr[0] << 24) & 0xFF000000);

		return address;
	}


	public static InetAddress ntoa(final byte[] addr)
	{
		try
		{
			if (addr.length == 4 || addr.length == 16)
			{
				return InetAddress.getByAddress(addr);
			}
			else
			{
				throw new IllegalArgumentException("a byte[] address for ntoa must be 4 bytes (ipv4) or 16 bytes (ipv6)");
			}
		}
		catch (UnknownHostException e)
		{
			// will never be thrown since we check the length manually
			throw new Error(e);
		}

	}


	/**
	 * Converts numeric address to an InetAddress
	 *
	 * @param address
	 *
	 * @return
	 */
	public static InetAddress ntoa(final int address)
	{
		try
		{
			final byte[] addr = new byte[4];

			addr[0] = (byte) ((address >>> 24) & 0xFF);
			addr[1] = (byte) ((address >>> 16) & 0xFF);
			addr[2] = (byte) ((address >>> 8) & 0xFF);
			addr[3] = (byte) (address & 0xFF);

			return InetAddress.getByAddress(addr);
		}
		catch (UnknownHostException e)
		{
			// will never be thrown
			throw new Error(e);
		}
	}


	/**
	 * Parses an IP address, throwing an {@link IllegalArgumentException} (rather than an {@link UnknownHostException}) if it is
	 * invalid
	 *
	 * @param ip
	 * 		the IP - must not be null or an empty string. should be a valid IP address (eg. 1.2.3.4)
	 *
	 * @return an InetAddress
	 */
	public static InetAddress parse(final String ip)
	{
		if (ip == null || ip.isEmpty())
			throw new IllegalArgumentException("A null or empty string is not a valid IP address!");

		try
		{
			return InetAddress.getByName(ip);
		}
		catch (Throwable t)
		{
			throw new IllegalArgumentException("Not a valid IP address: " + ip, t);
		}
	}


	/**
	 * Determines whether a particular IP address is publicly routable on the internet
	 *
	 * @param addrIP
	 *
	 * @return
	 *
	 * @deprecated use isPubliclyRoutable(java.net.InetAddress)
	 */
	@Deprecated
	public static boolean isPublicallyRoutable(final InetAddress addrIP)
	{
		return isPubliclyRoutable(addrIP);
	}


	/**
	 * Determines whether a particular IP address is publicly routable on the internet
	 *
	 * @param addrIP
	 *
	 * @return
	 */
	public static boolean isPubliclyRoutable(final InetAddress addrIP)
	{
		if (addrIP == null)
			throw new NullPointerException("isPubliclyRoutable requires an IP address be passed to it!");
		return !addrIP.isSiteLocalAddress() && !addrIP.isLinkLocalAddress() && !addrIP.isLoopbackAddress();
	}
}
