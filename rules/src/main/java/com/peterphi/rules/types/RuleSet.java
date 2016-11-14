package com.peterphi.rules.types;

import ognl.OgnlContext;
import ognl.OgnlException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * A RuleSet is composed of an input (which performs an ognl command on the rule sets variables and optionally filters the
 * result)
 * and a set of individual rules with associated commands to be carried out if the input matches their condition
 * <p/>
 * Created by bmcleod on 08/09/2016.
 */
@XmlRootElement(name = "ruleset")
@XmlType(name = "RuleSetType")
public class RuleSet
{
	@XmlAttribute(name = "id", required = true)
	public String id;

	@XmlElement(name = "input", required = true)
	public Input input;

	@XmlElement(name = "rule")
	public List<Rule> rules = new ArrayList<>();


	public void runInput(OgnlContext context) throws OgnlException
	{
		input.runCommands(context);

		for (Rule rule : rules)
		{
			//mark the input as run for each rule
			rule.setInputRun(true);
		}
	}
}
