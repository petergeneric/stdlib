package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name="inherit_one")
@DiscriminatorValue("one")
class MyChildObject1 extends MyBaseObject
{
}
