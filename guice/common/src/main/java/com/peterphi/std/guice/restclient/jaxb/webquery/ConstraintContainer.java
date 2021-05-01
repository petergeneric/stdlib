package com.peterphi.std.guice.restclient.jaxb.webquery;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public interface ConstraintContainer<T>
{
	T add(WQConstraintLine line);


	/**
	 * Assert that a field equals one of the provided values. Implicitly creates a new OR group if multiple values are supplied
	 *
	 * @param field
	 * @param values
	 * @return
	 */
	default T eq(final String field, final Object... values)
	{
		if (values == null)
		{
			return add(WQConstraint.eq(field, null));
		}
		else if (values.length == 1)
		{
			return add(WQConstraint.eq(field, values[0]));
		}
		else if (values.length > 1)
		{
			final WQGroup or = WQGroup.newOr();

			for (Object value : values)
				or.eq(field, value);

			return add(or);
		}
		else
		{
			return add(null);
		}
	}

	/**
	 * Assert that a field equals one of the provided values. Implicitly creates a new OR group if multiple values are supplied.
	 * At least one value must be supplied.
	 *
	 * @param field
	 * @param values
	 * @return
	 */
	default T eq(final String field, final Collection<?> values)
	{
		if (values == null)
			throw new IllegalArgumentException("Must supply at least one value to .eq when passing a Collection");
		else if (values.size() == 1)
			return eq(field, values.stream().findFirst().get());
		else
		{
			final WQGroup or = WQGroup.newOr();

			for (Object value : values)
				or.eq(field, value);

			return add(or);
		}
	}


	default T in(final String field, final Object... values)
	{
		return in(field, Arrays.asList(values));
	}

	default T in(final String field, final Collection<?> values)
	{
		if (values == null || values.isEmpty())
			throw new IllegalArgumentException("Must supply at least one value to IN when passing a Collection");
		else
			return add(WQConstraint.in(field, values));
	}

	default T notIn(final String field, final Object... values)
	{
		return notIn(field, Arrays.asList(values));
	}

	default T notIn(final String field, final Collection<?> values)
	{
		if (values == null || values.isEmpty())
			throw new IllegalArgumentException("Must supply at least one value to NOT IN when passing a Collection");
		else
			return add(WQConstraint.notIn(field, values));
	}

	default T neq(final String field, final Object value)
	{
		return add(WQConstraint.neq(field, value));
	}


	default T isNull(final String field)
	{
		return add(WQConstraint.isNull(field));
	}


	default T isNotNull(final String field)
	{
		return add(WQConstraint.isNotNull(field));
	}


	default T lt(final String field, final Object value)
	{
		return add(WQConstraint.lt(field, value));
	}


	default T le(final String field, final Object value)
	{
		return add(WQConstraint.le(field, value));
	}


	default T gt(final String field, final Object value)
	{
		return add(WQConstraint.gt(field, value));
	}


	default T ge(final String field, final Object value)
	{
		return add(WQConstraint.ge(field, value));
	}


	default T contains(final String field, final Object value)
	{
		return add(WQConstraint.contains(field, value));
	}


	default T startsWith(final String field, final Object value)
	{
		return add(WQConstraint.startsWith(field, value));
	}

	default T notStartsWith(final String field, final Object value)
	{
		return add(WQConstraint.notStartsWith(field, value));
	}


	default T range(final String field, final Object from, final Object to)
	{
		return add(WQConstraint.range(field, from, to));
	}

	default T eqRef(final String field, final String field2)
	{
		return add(WQConstraint.eqRef(field, field2));
	}


	default T neqRef(final String field, final String field2)
	{
		return add(WQConstraint.neqRef(field, field2));
	}


	default T leRef(final String field, final String field2)
	{
		return add(WQConstraint.leRef(field, field2));
	}


	default T ltRef(final String field, final String field2)
	{
		return add(WQConstraint.ltRef(field, field2));
	}


	default T geRef(final String field, final String field2)
	{
		return add(WQConstraint.geRef(field, field2));
	}


	default T gtRef(final String field, final String field2)
	{
		return add(WQConstraint.gtRef(field, field2));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Sub-groups
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Construct a new OR group and return it for method chaining
	 *
	 * @return
	 */
	default WQGroup and()
	{
		final WQGroup and = WQGroup.newAnd();

		add(and);

		return and;
	}


	/**
	 * Construct a new OR group and return it for method chaining
	 *
	 * @return
	 */
	default WQGroup or()
	{
		final WQGroup or = WQGroup.newOr();

		add(or);

		return or;
	}


	/**
	 * Construct a new AND group, using the supplier to add the constraints to the group. Returns the original {@link WQGroup} for
	 * method chaining
	 *
	 * @param consumer
	 * @return
	 */
	default T and(Consumer<WQGroup> consumer)
	{
		final WQGroup and = WQGroup.newAnd();

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(and);

		return add(and);
	}


	/**
	 * Construct a new OR group, using the supplier to add the constraints to the group. Returns the original {@link WQGroup} for
	 * method chaining
	 *
	 * @param consumer
	 * @return
	 */
	default T or(Consumer<WQGroup> consumer)
	{
		final WQGroup or = WQGroup.newOr();

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(or);

		return add(or);
	}
}
