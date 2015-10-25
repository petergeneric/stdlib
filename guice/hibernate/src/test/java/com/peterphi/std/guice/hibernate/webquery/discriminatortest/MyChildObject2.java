package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name="inherit_two")
@DiscriminatorValue("two")
class MyChildObject2 extends MyBaseObject
{
}
