package com.tuncaysenturk.jira.plugins.jtp.twitter.startup;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.opensymphony.module.propertyset.PropertySet;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.twitter.JiraTwitterStream;

public class JiraTwitterStreamStartup implements LifecycleAware {
	private static transient Logger logger = Logger.getLogger(JiraTwitterStreamStartup.class);
	final private JiraTwitterStream jiraTwitterStream;
	
	public JiraTwitterStreamStartup(JiraTwitterStream jiraTwitterStream) {
		this.jiraTwitterStream = jiraTwitterStream;
	}
	
	@Override
	public void onStart() {
		logger.info(JTPConstants.LOG_PRE + "Trying to stream twitter account");
		setAccessTokens();
		if (!jiraTwitterStream.isValidAccessToken()) {
			logger.error(JTPConstants.LOG_PRE + "Access tokens are not set. Please set parameters from " +
					"Administration > Plugins > Jira Twitter Plugin Configure section");
			jiraTwitterStream.setListening(false);
		} else {
			jiraTwitterStream.streamUser();
			logger.info(JTPConstants.LOG_PRE + "Successfully listening twitter account for new issues");
		}
	}
	
	private void setAccessTokens() {
		PropertySet propGet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
		String consumerKey = propGet.getString("consumerKey");
		String consumerSecret = propGet.getString("consumerSecret");
		String accessToken = propGet.getString("accessToken");
		String accessSecret = propGet.getString("accessSecret");
		jiraTwitterStream.setAccessTokens(consumerKey, consumerSecret, accessToken, accessSecret);
	}

}
