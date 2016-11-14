package com.peterphi.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by bmcleod on 08/09/2016.
 */
@XmlRootElement(name = "sometype")
public class SomeJaxbType
{
	@XmlElement
	public String field;
}
