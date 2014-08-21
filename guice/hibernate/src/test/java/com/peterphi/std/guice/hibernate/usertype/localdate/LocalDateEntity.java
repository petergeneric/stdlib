package com.peterphi.std.guice.hibernate.usertype.localdate;

import org.joda.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "test_entity")
class LocalDateEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Column(name = "some_date", nullable = false)
	public LocalDate someDate = new LocalDate();

	@Column(name = "some_date_nullable", nullable = true)
	public LocalDate someNullDate = null;
}
