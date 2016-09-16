package com.peterphi.rules;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.std.threading.ThreadRenameCallableWrap;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.Callable;

/**
 * Created by bmcleod on 16/09/2016.
 */
public class RuleProcessTask<Void> implements Callable<Void>
{
	final RulesEngineImpl rulesEngine;
	final RuleSet ruleSet;
	final OgnlContext ruleSetContext;
	final Rule rule;
	final DateTime created = DateTime.now();

	//for anyone wanting to wait on our result, do not do anything with this future in the call() method!!
	ListenableFuture<Object> future;

	private final static Logger log = Logger.getLogger(RuleProcessTask.class);


	public RuleProcessTask(RulesEngineImpl rulesEngine, final RuleSet ruleSet, final Rule rule, final OgnlContext ruleSetContext)
	{
		this.rulesEngine = rulesEngine;
		this.ruleSet = ruleSet;
		this.rule = rule;
		this.ruleSetContext = ruleSetContext;
	}


	@Override
	public Void call() throws Exception
	{
		try
		{
			if (rule.matches(ruleSetContext))
			{
				OgnlContext rContext = rulesEngine.createContext(ruleSetContext);
				rule.runCommands(rContext);
			}
		}
		catch (Exception e)
		{
			log.error("Exception processing rule " + rule.id, e);
			throw e;
		}
		finally
		{
			rulesEngine.taskOver(rule.id);
		}

		return null;
	}


	public Rule getRule()
	{
		return rule;
	}


	public DateTime getCreated()
	{
		return created;
	}


	public ListenableFuture<Object> getFuture()
	{
		return future;
	}


	public void submit(final ListeningExecutorService executorService)
	{
		future = executorService.submit(new ThreadRenameCallableWrap<Object>("Rule " + rule.id, (Callable<Object>) this));
	}
}
