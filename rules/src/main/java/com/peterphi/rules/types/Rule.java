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
 * A rule has a conditional ognl expression which if true results in its commands being executed
 * <p/>
 * Created by bmcleod on 08/09/2016.
 */
@XmlType(name = "RuleType")
@XmlRootElement(name = "rule")
public class Rule
{
	@XmlAttribute(name = "condition", required = true)
	public OgnlCommand condition;

	@XmlElement(name = "command", required = false)
	public List<OgnlCommand> commands = new ArrayList<>();

	private Object inputObj = null;
	private Boolean matches = null;


	public void setInputObj(final Object inputObj)
	{
		this.inputObj = inputObj;
	}


	public boolean matches(final OgnlContext ognlContext) throws OgnlException
	{
		if (matches == null)
		{
			return assessMatch(ognlContext);
		}
		else
		{
			return matches;
		}
	}


	/**
	 * returns true if the rules condition holds, should not be called until after the rule sets input has been produced
	 *
	 * @param ognlContext
	 *
	 * @return
	 *
	 * @throws OgnlException
	 */
	public boolean assessMatch(final OgnlContext ognlContext) throws OgnlException
	{

		if (inputObj == null)
		{
			throw new IllegalArgumentException("Attempted to match on a rule whose rulesets input has not yet been produced");
		}

		ognlContext.put("input", inputObj);
		final Object result = condition.run(ognlContext, ognlContext);
		ognlContext.remove("input");

		if (result == null || !Boolean.class.isAssignableFrom(result.getClass()))
		{
			throw new IllegalArgumentException("Expression " + condition.getOriginalExpression() +
			                                   " did not return a boolean value");
		}

		matches = (Boolean) result;

		return matches;
	}


	public void runCommands(final OgnlContext context) throws OgnlException
	{
		if (matches)
		{
			context.put("input", inputObj);
			for (OgnlCommand command : commands)
			{
				command.run(context, context);
			}
			context.remove("input");
		}
		else
		{
			throw new IllegalArgumentException("Tried to run commands for a rule whose conditions were not met!");
		}
	}
}
