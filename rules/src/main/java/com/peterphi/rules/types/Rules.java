package com.peterphi.rules.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Rules definition document
 * <p/>
 * Created by bmcleod on 08/09/2016.
 */
@XmlRootElement(name = "rules")
@XmlType(name = "RulesType")
public class Rules
{
	/**
	 * The variables available to our rules
	 */
	@XmlElementWrapper(name = "variables")
	@XmlElements({@XmlElement(name = "restService", type = RestServiceVar.class),
	              @XmlElement(name = "localObject", type = LocalObjectVar.class),
	              @XmlElement(name = "jaxbObject", type = JaxbObjectVar.class),
	              @XmlElement(name = "stringObject", type = StringObjectVar.class),
	              @XmlElement(name = "integerObject", type = IntegerObjectVar.class)})
	public List<Variable> variables = new ArrayList<>();

	/**
	 * A collection of rule sets, the rules in each set act upon the same input
	 */
	@XmlElement(name = "ruleset")
	public List<RuleSet> ruleSets = new ArrayList<>();
}