package com.peterphi.rules.types;

import ognl.OgnlContext;
import ognl.OgnlException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by bmcleod on 08/09/2016.
 */
@XmlType(name = "RuleInput")
@XmlRootElement(name = "rule")
public class Input
{
	@XmlElement(name = "command", required = true)
	public OgnlCommand command;
	@XmlElement(name = "filter", required = false)
	public OgnlCommand filter;


	public Object run(final OgnlContext context) throws OgnlException
	{
		Object commandOutput = command.run(context, context);

		if (filter != null)
		{
			//put the command output into the context to allow the filter to act up on
			context.put("input", commandOutput);
			Object filterOutput = filter.run(context, context);
			//clean up the added input
			context.remove("input");

			return filterOutput;
		}
		else
		{
			return commandOutput;
		}
	}
}
