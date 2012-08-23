package com.tuncaysenturk.jira.plugins.jtp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public class TweetPostFunction extends AbstractJiraFunctionProvider {
	private static final Logger log = LoggerFactory.getLogger("com.atlassian."
			+ TweetPostFunction.class.getName());

	private TweetBuilder tweetBuilder;
	private ThirdPartyPluginLicenseStorageManager licenseStorageManager;

	public TweetPostFunction(TweetBuilder emailBuilder,
			ThirdPartyPluginLicenseStorageManager licenseStorageManager) {
		this.tweetBuilder = emailBuilder;
		this.licenseStorageManager = licenseStorageManager;
	}

	@SuppressWarnings("rawtypes")
	public void execute(Map transientVars, Map args, PropertySet ps)
			throws WorkflowException {
		MutableIssue issue = getIssue(transientVars);

		try {
			if (!LicenseValidator.isValid(licenseStorageManager))
				log.error(JTPConstants.LOG_PRE + "Invalid license");
			else {
				PropertySet propSet = ComponentManager.getComponent(
						PropertiesManager.class).getPropertySet();
				if (propSet.getBoolean("stopTweeting"))
					log.warn(JTPConstants.LOG_PRE + "There is a stop request, so I'll do nothing");
				else {
					TweetDefinition tweetDefiniton = buildTweet(issue,
							transientVars, args, ps);
					tweetBuilder.buildAndSendTweet(tweetDefiniton);
				}
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