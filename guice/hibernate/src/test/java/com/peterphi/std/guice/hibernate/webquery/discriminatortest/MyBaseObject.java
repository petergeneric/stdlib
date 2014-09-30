package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
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
