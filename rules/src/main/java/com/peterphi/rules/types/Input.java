package com.peterphi.rules.types;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmcleod on 08/09/2016.
 */
@XmlType(name = "RuleInput")
@XmlRootElement(name = "rule")
public class Input
{
	@XmlElement(name = "command", required = false)
	public List<OgnlCommand> commands = new ArrayList<>();

	public void runCommands(final OgnlContext context) throws OgnlException
	{
		for (OgnlCommand command : commands)
		{
			command.run(context, context);
		}
	}

}
