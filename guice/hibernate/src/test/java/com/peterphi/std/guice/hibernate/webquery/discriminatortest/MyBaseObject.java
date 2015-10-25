package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name="inherit_base")
@Inheritance(strategy = InheritanceType.JOINED)
// DiscriminatorColum has been commented out to work around a bug in Hibernate - see https://hibernate.atlassian.net/browse/HHH-9501
// A Discriminator is not required for this functionality in our library, but I've left this reference here to make it easier to figure
// Out what's going wrong in the future
//@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
abstract class MyBaseObject
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;


	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" +
		       "id=" + id +
		       '}';
	}
}
