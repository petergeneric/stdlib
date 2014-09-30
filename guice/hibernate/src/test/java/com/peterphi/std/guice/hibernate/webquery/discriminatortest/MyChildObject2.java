package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("two")
class MyChildObject2 extends MyBaseObject
{
}
