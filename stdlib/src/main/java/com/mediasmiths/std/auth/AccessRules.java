package com.mediasmiths.std.auth;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * Title: AccessRules
 * </p>
 * 
 * <p>
 * Description: AccessRules stores a list of DNs and an application order which is either allow, deny or deny, allow
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 */
public final class AccessRules implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean allowRules;
	private Vector<String> DNs = new Vector<String>();


	/**
	 * AccessRules creates an empty collection of AccessRules that are applied in allow,deny if allowRules is true and deny, allow otherwise
	 * 
	 * @param allowRules boolean
	 */
	public AccessRules(boolean allowRules) {
		this.allowRules = allowRules;
	}


	/**
	 * AccessRules creates an collection of AccessRules initialised to contain the DNs stored in defaultDNs that are applied in allow,deny if allowRules is true and deny, allow otherwise
	 * 
	 * @param allowRules boolean true if the rules are for Allow (otherwise they are for Deny)
	 * @param defaultDNs List[String] The list of DNs in this rule
	 */
	public AccessRules(boolean allowRules, List<String> defaultDNs) {
		this.allowRules = allowRules;
		DNs.addAll(defaultDNs);
	}


	public boolean isPermitOrder() {
		return getAllowDenyOrder();
	}


	public boolean isDenyOrder() {
		return getDenyAllowOrder();
	}


	/**
	 * setAllowDenyOrder sets the rule order to deny, allow
	 */
	public void setDenyAllowOrder() {
		this.allowRules = false;
	}


	public boolean getDenyAllowOrder() {
		return !this.allowRules;
	}


	/**
	 * setAllowDenyOrder sets the rule order to allow, deny
	 */
	public void setAllowDenyOrder() {
		this.allowRules = true;
	}


	public boolean getAllowDenyOrder() {
		return this.allowRules;
	}


	/**
	 * addDN adds the argument DN to the list of DNs in this rule set.
	 * 
	 * @param newDN String
	 */
	public void addDN(String newDN) {
		this.DNs.add(newDN);
	}


	/**
	 * Removes an individual DN from the current DN list for this ruleset
	 * 
	 * @param dn
	 */
	public void removeDN(String dn) {
		this.DNs.remove(dn);
	}


	/**
	 * addAllDNs adds all of the DNs in the argument list to the current rule set.
	 * 
	 * @param dnList List
	 */
	public void addAllDNs(Collection<String> dnList) {
		this.DNs.addAll(dnList);
	}


	/**
	 * Removes a number of DNs from the current DN list for this ruleset
	 * 
	 * @param dnList
	 */
	public void removeAllDNs(Collection<String> dnList) {
		this.DNs.removeAll(dnList);
	}


	/**
	 * Empties the list of DNs for this ruleset
	 */
	public void removeAllDNs() {
		this.DNs.clear();
	}


	/**
	 * isPermittedAccess returns true if the argument DN is permitted access given the stored DNs and the way those rules are to be applied (i.e. allow, deny or deny, allow)
	 * 
	 * @param dn String
	 * @return boolean
	 */
	public boolean isPermittedAccess(String dn) {
		for (String dnEntry : DNs) {
			if (dnEntry.endsWith("/") && dnEntry.substring(dn.length()).equalsIgnoreCase(dn)) {
				return allowRules;
			}
			else if (dnEntry.equalsIgnoreCase(dn)) {
				return allowRules;
			}
		}

		return !allowRules;
	}


	@Override
	public Object clone() {
		return new AccessRules(allowRules, DNs);
	}
}
