package com.peterphi.rules;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.rules.types.OgnlCommand;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.ThreadRenameCallableWrap;
import ognl.MethodFailedException;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesEngineImpl implements RulesEngine, StoppableService
{
	@Inject
	Injector injector;

	private final static Logger log = Logger.getLogger(RulesEngineImpl.class);

	private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	//this map records the rules that are currently being processed, the key is the rules id
	//RuleProcessTasks call this engine in a finally block to remove their entry when complete
	private ConcurrentHashMap<String, RuleProcessTask> activeTasks = new ConcurrentHashMap();


	@Override
	public Map<String, Object> prepare(final Rules rules)
	{
		Map<String, Object> vars = rules.variables.stream().collect(Collectors.toMap(v -> v.getName(), v -> prepare(v)));

		return vars;
	}


	OgnlContext createContext(final Map<String, Object> vars)
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

		Set<String> knownIds = new HashSet<>();

		for (RuleSet ruleSet : rules.ruleSets)
		{

			if (knownIds.contains(ruleSet.id))
			{
				throw new IllegalArgumentException("Duplicate id " + ruleSet.id);
			}

			for (OgnlCommand command : ruleSet.input.commands)
			{
				validate(ruleSet.id + " - input", command, results);
			}

			for (Rule rule : ruleSet.rules)
			{
				if (StringUtils.isEmpty(rule.id))
				{
					throw new IllegalArgumentException("Rule with condition " + rule.condition + " has no id!");
				}

				if (knownIds.contains(rule.id))
				{
					throw new IllegalArgumentException("Duplicate id " + rule.id);
				}

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

		for (RuleSet rs : rules.ruleSets)
		{
			try
			{
				log.debug("Assessing input for ruleset : " + rs.id);

				OgnlContext rsContext = createContext(vars);
				rs.runInput(rsContext);

				for (Rule rule : rs.rules)
				{
					if (StringUtils.isEmpty(rule.id))
					{
						throw new IllegalArgumentException("Rule with condition " + rule.condition + " has no id!");
					}

					final RuleProcessTask task = new RuleProcessTask(this, rs, rule, rsContext);

					synchronized (activeTasks)
					{
						//if not currently being processed
						if (!activeTasks.containsKey(rule.id))
						{
							try
							{
								activeTasks.put(rule.id, task);
								task.submit(executorService);
							}
							catch (Exception e)
							{
								log.error("Error submitting rule for execution ", e);
								activeTasks.remove(rule.id);
							}
						}
					}
				}
			}
			catch (MethodFailedException mfe)
			{

				if (!ignoreMethodErrors)
				{
					throw mfe;
				}

				log.warn("Method failed for ruleset " + rs.id, mfe);
			}
		}
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
		log.debug("Assessing input for ruleset : " + ruleSet.id);

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


	void waitForAllRuleTasks() throws InterruptedException
	{

		int activeTasks = this.activeTasks.entrySet().size();

		while (activeTasks > 0)
		{
			Thread.sleep(100l);
			activeTasks = this.activeTasks.entrySet().size();
		}
	}


	void taskOver(final String id)
	{
		activeTasks.remove(id);
	}


	@Override
	public void shutdown()
	{
		executorService.shutdown();
	}


	@Override
	public Collection<RuleProcessTask> getActive()
	{
		return activeTasks.values();
	}


	@Override
	public RuleProcessTask getActiveById(final String id)
	{
		return activeTasks.get(id);
	}
}