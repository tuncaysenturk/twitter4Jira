package com.tuncaysenturk.jira.plugins.jtp;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.I18nHelper;

public class TweetDefinitionImpl implements TweetDefinition {
	private Issue issue;
	private User remoteUser;
	private String tweetMessageTemplate;
	private I18nHelper i18nHelper;

	public TweetDefinitionImpl(Issue issue, 
			User remoteUser, 
			String tweetMessageTemplate) {
		this.issue = issue;
		this.remoteUser = remoteUser;
		this.tweetMessageTemplate = tweetMessageTemplate;
	}
	
	public I18nHelper getI18n() {
		return this.i18nHelper;
	}

	public Issue getIssueObject() {
		return this.issue;
	}

	public User getRemoteUser() {
		return this.remoteUser;
	}

	public String getTweetMessageTemplate() {
		return tweetMessageTemplate;
	}

	public void setTweetMessageTemplate(String tweetMessagetemplate) {
		this.tweetMessageTemplate = tweetMessagetemplate;
	}
}