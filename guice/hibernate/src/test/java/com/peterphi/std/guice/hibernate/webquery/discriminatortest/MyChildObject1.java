package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.UUID;

@Entity(name = "inherit_one")
@DiscriminatorValue("one")
class MyChildObject1 extends MyBaseObject
{
	@Column(nullable = true)
	String someId = UUID.randomUUID().toString();
}
