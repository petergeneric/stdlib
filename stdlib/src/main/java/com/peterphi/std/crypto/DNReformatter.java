package com.peterphi.std.crypto;

import java.util.*;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;

/**
 * Reformats a Distinguished Name into one of a variety of formats
 * 
 */
public class DNReformatter {
	static final DERObjectIdentifier[] ORDER_DESCENDING = new DERObjectIdentifier[] { X509Name.C, X509Name.ST, X509Name.L,
		X509Name.O, X509Name.OU, X509Name.CN };
	static final DERObjectIdentifier[] ORDER_ASCENDING = new DERObjectIdentifier[] { X509Name.CN, X509Name.OU, X509Name.O,
		X509Name.L, X509Name.ST, X509Name.C };

	/**
	 * The default style (descending (CN last), comma-separated
	 */
	public static final DNReformatter DEFAULT = new DNReformatter(ORDER_DESCENDING, ", ");

	/**
	 * LDAP's default style (ascending (CN first), comma-separated)
	 */
	public static final DNReformatter LDAP = new DNReformatter(ORDER_ASCENDING, ", ");

	/**
	 * The active directory style (descending (CN last), slash-separated with a leading slash)
	 */
	public static final DNReformatter ACTIVE_DIRECTORY = new DNReformatter(ORDER_DESCENDING, "/", "/", null, null, null);
	/**
	 * Synonym for ACTIVE_DIRECTORY (descending (CN last), slash-separated with a leading slash)
	 */
	public static final DNReformatter DCE = ACTIVE_DIRECTORY;

	/**
	 * Synonym for ACTIVE_DIRECTORY, the style that Globus applications tend to use (descending (CN last), slash-separated with a leading slash)
	 */
	public static final DNReformatter GLOBUS = ACTIVE_DIRECTORY;

	private static final DERObjectIdentifier[] ORDER_DEFAULT = ORDER_DESCENDING;

	private final DERObjectIdentifier[] order;
	private final String field_separator;
	private final String begin_dn;
	private final String end_dn;
	private final String begin_value;
	private final String end_value;

	/**
	 * An individual piece of DN information
	 * 
	 */
	private static class DNInformation {
		public final DERObjectIdentifier oid;
		public final String value;


		public DNInformation(DERObjectIdentifier oid, String value) {
			this.oid = oid;
			this.value = value;
		}
	}


	public DNReformatter() {
		this(ORDER_DEFAULT, null, null, null, null, null);
	}


	public DNReformatter(DERObjectIdentifier[] order) {
		this(order, null, null, null, null, null);
	}


	public DNReformatter(DERObjectIdentifier[] order, String field_separator) {
		this(order, field_separator, null, null, null, null);
	}


	public DNReformatter(
			DERObjectIdentifier[] order,
			String field_separator,
			String begin_dn,
			String end_dn,
			String begin_value,
			String end_value) {
		this.order = order;
		this.field_separator = field_separator;
		this.begin_dn = begin_dn;
		this.end_dn = end_dn;
		this.begin_value = begin_value;
		this.end_value = end_value;
	}


	public DNReformatter(
			String[] symbols,
			String field_separator,
			String begin_dn,
			String end_dn,
			String begin_value,
			String end_value) {
		if (symbols == null || symbols.length == 0)
			throw new IllegalArgumentException("You must specify a format!");

		order = new DERObjectIdentifier[symbols.length];
		for (int i = 0; i < symbols.length; i++) {
			order[i] = oid(symbols[i]);
		}

		this.field_separator = field_separator;
		this.begin_dn = begin_dn;
		this.end_dn = end_dn;
		this.begin_value = begin_value;
		this.end_value = end_value;
	}


	/**
	 * This is a convenience method which calls <code>reformatToString</code> and wraps the result in an X500Principal; this method will fail with an IllegalArgumentException if the output of this formatter is not parseable by X500Principal (eg. the ACTIVE_DIRECTORY format)
	 * 
	 * @param dn
	 * @return
	 */
	public X500Principal reformat(X500Principal p) {
		return new X500Principal(reformatToString(p));
	}


	/**
	 * This is a convenience method which calls <code>reformatToString</code> and wraps the result in an X509Name; this method will fail with an IllegalArgumentException if the output of this formatter is not parseable by X509Name (eg. the ACTIVE_DIRECTORY format)
	 * 
	 * @param dn
	 * @return
	 */
	public X509Name reformat(X509Name dn) {
		return new X509Name(reformatToString(dn));
	}


	/**
	 * Reformats an X500Principal; the result may not be a valid X500Principal, so this method returns a String
	 * 
	 * @param p
	 * @return
	 */
	public String reformatToString(X500Principal p) {
		X509Name dn = new X509Name(p.getName(X500Principal.RFC2253));

		return format(order(parse(dn)));
	}


	/**
	 * Reformats an X509Name; the result may not be a valid X509Name, so this method returns a String
	 * 
	 * @param dn
	 * @return
	 */
	public String reformatToString(X509Name dn) {
		return format(order(parse(dn)));
	}


	protected List<DNInformation> order(List<DNInformation> dn) {
		List<DNInformation> ordered = new ArrayList<DNInformation>(dn.size());

		for (DERObjectIdentifier oid : order) {
			for (DNInformation nfo : dn)
				if (oid.equals(nfo.oid)) {
					ordered.add(nfo);
					break;
				}
		}

		return ordered;
	}


	/**
	 * Formats an ordered DNInformation list
	 * 
	 * @param dn
	 * @return
	 */
	protected String format(List<DNInformation> dn) {
		StringBuilder sb = new StringBuilder();

		if (begin_dn != null)
			sb.append(begin_dn);

		for (int i = 0; i < dn.size(); i++) {
			DNInformation nfo = dn.get(i);

			String symbol = symbol(nfo.oid);

			if (symbol != null) {
				if (i != 0) // For everything but the 1st field we need to put a separator between the previous and new entry
					sb.append(field_separator);

				sb.append(symbol);
				sb.append("=");
				if (begin_value != null)
					sb.append(begin_value);
				sb.append(nfo.value);
				if (end_value != null)
					sb.append(end_value);
			}
		}

		if (end_dn != null)
			sb.append(end_dn);

		return sb.toString();
	}


	private String symbol(DERObjectIdentifier oid) {
		Object val = X509Name.DefaultSymbols.get(oid);

		if (val == null)
			return null;
		else
			return (String) val;
	}


	private DERObjectIdentifier oid(String symbol) {
		Object val = X509Name.DefaultLookUp.get(symbol.toLowerCase());

		if (val == null)
			return null;
		else
			return (DERObjectIdentifier) val;
	}


	@SuppressWarnings("unchecked")
	protected List<DNInformation> parse(X509Name n) {
		List<DNInformation> nfo = new ArrayList<DNInformation>();

		List<DERObjectIdentifier> oids = n.getOIDs();
		List<String> values = n.getValues();

		int size = Math.min(oids.size(), values.size());
		for (int i = 0; i < size; i++) {
			DERObjectIdentifier oid = oids.get(i);

			// Only look at interesting OIDs:
			if (interested(oid)) {
				nfo.add(new DNInformation(oid, values.get(i)));
			}
		}

		return nfo;
	}


	protected boolean interested(DERObjectIdentifier oid) {
		if (oid == null)
			return false;

		for (DERObjectIdentifier id : order)
			if (id.equals(oid))
				return true;
		return false;
	}
}
