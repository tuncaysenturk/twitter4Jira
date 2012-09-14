package com.tuncaysenturk.jira.plugins.jtp.twitter.startup;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.twitter.JiraTwitterStream;
import com.tuncaysenturk.jira.plugins.jtp.twitter.TwitterStreamHolder;
import com.tuncaysenturk.jira.plugins.jtp.util.ExceptionMessagesUtil;

public class JiraTwitterStreamStartup implements LifecycleAware {
	private static transient Logger logger = Logger.getLogger(JiraTwitterStreamStartup.class);
	final private JiraTwitterStream jiraTwitterStream;
	private TwitterStreamHolder twitterStreamHolder;
	
	public JiraTwitterStreamStartup(JiraTwitterStream jiraTwitterStream,
			TwitterStreamHolder twitterStreamHolder) {
		this.jiraTwitterStream = jiraTwitterStream;
		this.twitterStreamHolder = twitterStreamHolder;
	}
	
	@Override
	public void onStart() {
		logger.info(JTPConstants.LOG_PRE + "Trying to stream twitter account");
		if (!jiraTwitterStream.isValidAccessToken()) {
			logger.error(JTPConstants.LOG_PRE + "Access tokens are not set. Please set parameters from " +
					"Administration > Plugins > Jira Twitter Plugin Configure section");
			ExceptionMessagesUtil.addExceptionMessage("Access tokens are not set. Please set parameters in " +
					"Administration > Plugins > Jira Twitter Plugin Configure section");
		} else {
			jiraTwitterStream.startListener();
			twitterStreamHolder.addTwitterStream(jiraTwitterStream);
			logger.info(JTPConstants.LOG_PRE + "Successfully listening twitter account for new issues");
		}
	}
}
