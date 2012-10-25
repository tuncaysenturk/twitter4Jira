package com.tuncaysenturk.jira.plugins.jtp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.tuncaysenturk.jira.plugins.jtp.util.ExceptionMessagesUtil;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public class TweetPostFunction extends AbstractJiraFunctionProvider {
	private static final Logger log = LoggerFactory.getLogger("com.atlassian."
			+ TweetPostFunction.class.getName());

	private TweetBuilder tweetBuilder;
	private final LicenseValidator licenseValidator;

	public TweetPostFunction(TweetBuilder emailBuilder,
			LicenseValidator licenseValidator) {
		this.tweetBuilder = emailBuilder;
		this.licenseValidator = licenseValidator;
	}

	@SuppressWarnings("rawtypes")
	public void execute(Map transientVars, Map args, PropertySet ps)
			throws WorkflowException {
		MutableIssue issue = getIssue(transientVars);

		try {
			if (!licenseValidator.isValid()) {
				log.error(JTPConstants.LOG_PRE + "License problem, see configuration page");
				ExceptionMessagesUtil.addLicenseExceptionMessage();
			} else {
				TweetDefinition tweetDefiniton = buildTweet(issue,
						transientVars, args, ps);
				tweetBuilder.buildAndSendTweet(tweetDefiniton);
			}
		} catch (Exception e) {
			log.error(JTPConstants.LOG_PRE + e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	private TweetDefinition buildTweet(MutableIssue issue, Map transientVars,
			Map args, PropertySet ps) {

		TweetDefinition def = new TweetDefinitionImpl(issue, getCaller(
				transientVars, args), (String) args.get("tweetMessageTemplate"));

		return def;
	}

}