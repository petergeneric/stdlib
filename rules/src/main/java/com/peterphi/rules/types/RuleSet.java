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
	@XmlAttribute(name = "name", required = false)
	public String name;

	@XmlElement(name = "input", required = true)
	public Input input;

	@XmlElement(name = "rule")
	public List<Rule> rules = new ArrayList<>();

	private Object inputObj = null;


	public Object createInput(OgnlContext context) throws OgnlException
	{
		Object inputObj = input.run(context);

		for (Rule rule : rules)
		{
			//store the produced input against each of the rules for use when they are being matched and run
			rule.setInputObj(inputObj);
		}

		return inputObj;
	}


	public Object getInputObj()
	{
		return inputObj;
	}
}
