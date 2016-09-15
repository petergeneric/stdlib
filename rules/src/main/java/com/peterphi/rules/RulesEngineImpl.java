package com.peterphi.rules;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.rules.types.OgnlCommand;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import ognl.MethodFailedException;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesEngineImpl implements RulesEngine
{
	@Inject
	Injector injector;

	private final static Logger log = Logger.getLogger(RulesEngineImpl.class);


	@Override
	public Map<String, Object> prepare(final Rules rules)
	{
		Map<String, Object> vars = rules.variables.stream().collect(Collectors.toMap(v -> v.getName(), v -> prepare(v)));

		return vars;
	}


	@Override
	public OgnlContext createContext(final Map<String, Object> vars)
	{

		OgnlContext ognlContext = new OgnlContext();
		ognlContext.putAll(vars);
		ognlContext.put("logger", log);
		return ognlContext;
	}


	@Override
	public Map<String, String> validateSyntax(final Rules rules)
	{
		Map<String, String> results = new HashMap<>();

		for (RuleSet ruleSet : rules.ruleSets)
		{

			for (OgnlCommand command : ruleSet.input.commands)
			{
				validate(ruleSet.id + " - input", command, results);
			}

			for (Rule rule : ruleSet.rules)
			{
				validate(rule.id + " condition", rule.condition, results);
				for (OgnlCommand command : rule.commands)
				{
					validate(rule.id, command, results);
				}
			}
		}

		return results;
	}


	private void validate(final String identifier, final OgnlCommand command, final Map<String, String> results)
	{
		try
		{
			command.validate();
		}
		catch (OgnlException e)
		{
			log.info("OgnlException during validation " + e.getMessage(), e);
			results.put(identifier, e.getMessage());
		}
	}


	private Object prepare(final com.peterphi.rules.types.Variable v)
	{
		return v.getValue(injector);
	}


	/**
	 * returns a list of the rules that match from the supplied Rules document
	 *
	 * @param rules
	 *
	 * @return
	 */
	@Override
	public Map<RuleSet, List<Rule>> matching(Rules rules,
	                                         Map<String, Object> vars,
	                                         boolean ignoreMethodErrors) throws OgnlException
	{
		Map<RuleSet, List<Rule>> ret = new HashMap<>();

		for (RuleSet ruleSet : rules.ruleSets)
		{
			try
			{
				OgnlContext context = createContext(vars);

				List<Rule> matching = match(ruleSet, context);

				if (!matching.isEmpty())
				{
					ret.put(ruleSet, matching);
				}
			}
			catch (MethodFailedException mfe)
			{

				if (!ignoreMethodErrors)
				{
					throw mfe;
				}

				log.warn("Method failed for ruleset " + ruleSet.id, mfe);
			}
		}

		return ret;
	}


	@Override
	public Map<RuleSet, List<Rule>> matching(final Rules rules, final boolean ignoreMethodErrors) throws OgnlException
	{
		Map<String, Object> prepare = prepare(rules);
		return matching(rules, prepare, ignoreMethodErrors);
	}


	@Override
	public void execute(Map<RuleSet, List<Rule>> matchingrulesMap, Map<String, Object> vars) throws OgnlException
	{
		for (Map.Entry<RuleSet, List<Rule>> ruleSetListEntry : matchingrulesMap.entrySet())
		{
			RuleSet rs = ruleSetListEntry.getKey();
			List<Rule> rules = ruleSetListEntry.getValue();

			for (Rule rule : rules)
			{
				OgnlContext context = createContext(vars);
				rs.runInput(context);
				if (rule.matches(context))
				{
					rule.runCommands(context);
				}
				else
				{
					log.warn("Rule " + rule.id + " previously matched but doesn't any more");
				}
			}
		}
	}


	@Override
	public void run(final Rules rules, boolean ignoreMethodErrors) throws OgnlException
	{
		Map<String, Object> vars = prepare(rules);
		Map<RuleSet, List<Rule>> matching = matching(rules, vars, ignoreMethodErrors);
		execute(matching, vars);
	}


	/**
	 * returns a list of rules that match in the given rule set
	 *
	 * @param ruleSet
	 * @param ognlContext
	 *
	 * @return
	 */
	private List<Rule> match(final RuleSet ruleSet, final OgnlContext ognlContext) throws OgnlException
	{

		//get the rulesets id for logging/exception messages
		final String name = ruleSet.id == null ? "unnamed" : ruleSet.id;

		log.debug("Assessing input for ruleset : " + name);

		//run the input commands
		ruleSet.runInput(ognlContext);

		final List<Rule> ret = new ArrayList<>();

		//assess each rule against the input, return any that match
		for (Rule rule : ruleSet.rules)
		{
			Boolean bresult = rule.assessMatch(ognlContext);

			if (bresult)
			{
				log.debug(rule.condition.getOriginalExpression() + " matches");
				ret.add(rule);
			}
		}

		return ret;
	}
}
