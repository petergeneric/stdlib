package com.peterphi.rules.types;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class OgnlCommand
{

	String originalExpression;

	Object ognlExpression = null;


	public Object run(OgnlContext context, Object root) throws OgnlException
	{
		Object exp = getExpr();
		return Ognl.getValue(exp, context, root);
	}


	private Object getExpr() throws OgnlException
	{
		if (ognlExpression != null)
		{
			return ognlExpression;
		}

		ognlExpression = Ognl.parseExpression(originalExpression);
		return ognlExpression;
	}


	public String getOriginalExpression()
	{
		return originalExpression;
	}


	@XmlValue
	public void setOriginalExpression(final String originalExpression)
	{
		this.originalExpression = originalExpression;
		ognlExpression = null;
	}


	public void validate() throws OgnlException
	{
		getExpr();
	}
}
