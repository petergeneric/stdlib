package com.peterphi.rules;

import com.google.inject.ImplementedBy;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import ognl.OgnlException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by bmcleod on 08/09/2016.
 */
@ImplementedBy(RulesEngineImpl.class)
public interface RulesEngine
{

	/**
	 * prepares, matches and runs the supplied rules document
	 *
	 * if you want a full run of rules execution, this is the method to call
	 *
	 * @param rules
	 * @param ignoreMethodErrors
	 * 		- a flag that indicates method errors in individual rulesets should be logged then ignored when determining matches
	 */
	void run(Rules rules, boolean ignoreMethodErrors) throws OgnlException;

	/**
	 * Prepares the context for the supplied rules document, produces the required variables
	 *
	 * @param rules
	 *
	 * @return
	 */
	Map<String, Object> prepare(Rules rules);

	/**
	 * Validates the expressions in the given rules (but doesnt run them)
	 * @param rules
	 * @return
	 */
	Map<String, String> validateSyntax(Rules rules) throws OgnlException;

	/**
	 * Assess all rules in the supplied rules document, returns those that match
	 *
	 * @param rules
	 * @param vars
	 * @param ignoreMethodErrors
	 * 		- a flag that indicates method errors in individual rulesets should be logged then ignored when determining matches
	 *
	 * @return
	 *
	 * @throws OgnlException
	 */
	Map<RuleSet, List<Rule>> matching(Rules rules, Map<String, Object> vars, boolean ignoreMethodErrors) throws OgnlException;

	/**
	 * Assess all rules in the supplied rules document, returns those that match
	 *
	 * @param rules
	 * @param ignoreMethodErrors
	 * 		- a flag that indicates method errors in individual rulesets should be logged then ignored when determining matches
	 *
	 * @return
	 *
	 * @throws OgnlException
	 */
	Map<RuleSet, List<Rule>> matching(Rules rules, boolean ignoreMethodErrors) throws OgnlException;

	/**
	 * executes the supplied list of rules
	 *
	 * @param matchingrulesMap
	 * @param vars
	 */
	void execute(Map<RuleSet, List<Rule>> matchingrulesMap, Map<String, Object> vars) throws OgnlException;

	Collection<RuleProcessTask> getActive();

	RuleProcessTask getActiveById(String id);
}
