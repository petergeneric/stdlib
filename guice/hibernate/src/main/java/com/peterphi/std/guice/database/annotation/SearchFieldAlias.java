package com.peterphi.std.guice.database.annotation;

import com.peterphi.std.annotation.Doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If annotated on an Entity class, allows the creation of a Property/Relation which is, in fact, a reference to another Property
 * or Relation. This allows the entity object to be refactored to a degree without impacting on the search API (and without
 * requiring duplication of database columns with old+new names)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(SearchFieldAliasList.class)
public @interface SearchFieldAlias
{
	/**
	 * The name of the alias. This should not contain dots
	 *
	 * @return
	 */
	String name();

	/**
	 * The sub-path to which this alias refers. This may contain dots and may refer to a relation or to a property
	 *
	 * @return
	 */
	String aliasOf();

	/**
	 * Optional documentation about this alias
	 *
	 * @return
	 */
	Doc[] documentation() default {};
}
