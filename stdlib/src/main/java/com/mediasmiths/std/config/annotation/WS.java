package com.mediasmiths.std.config.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a field (which must be an Interface) is, in fact, a Webservice - to which a dynamic proxy should be created<br />
 * A special field, "impl" is read to determine what type of service this is. See WebServiceParser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface WS {

}
