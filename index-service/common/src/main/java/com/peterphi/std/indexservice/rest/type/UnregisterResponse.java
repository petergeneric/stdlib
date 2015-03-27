package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UnregisterResponse {
    @XmlAttribute
    public boolean wasSuccessful = true;
}
