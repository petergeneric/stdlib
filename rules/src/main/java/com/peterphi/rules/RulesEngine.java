package com.peterphi.rules;

import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.Rules;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.util.List;

/**
 * Created by bmcleod on 08/09/2016.
 */
public interface RulesEngine
{
	/**
	 * Prepares the context for the supplied rules document, produces the required variables
	 *
	 * @param rules
	 *
	 * @return
	 */
	OgnlContext prepare(Rules rules);

	/**
	 * Assess all rules in the supplied rules document, returns those that match
	 *
	 * @param rules
	 * @param context
	 * @param ignoreMethodErrors
	 * 		- a flag that indicates method errors in individual rulesets should be logged then ignored when determining matches
	 *
	 * @return
	 *
	 * @throws OgnlException
	 */
	List<Rule> matching(Rules rules, OgnlContext context, boolean ignoreMethodErrors) throws OgnlException;

	/**
	 * executes the supplied list of rules
	 *
	 * @param rules
	 * @param context
	 */
	void execute(List<Rule> rules, OgnlContext context) throws OgnlException;

	/**
	 * preapres, matches and runs the supplied rules document
	 *
	 * @param rules
	 * @param ignoreMethodErrors
	 * 		- a flag that indicates method errors in individual rulesets should be logged then ignored when determining matches
	 */
	void run(Rules rules, boolean ignoreMethodErrors) throws OgnlException;
}
