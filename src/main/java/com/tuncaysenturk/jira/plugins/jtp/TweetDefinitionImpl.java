package com.tuncaysenturk.jira.plugins.jtp;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

public class TweetDefinitionImpl implements TweetDefinition {
	private Issue issue;
	private ApplicationUser remoteUser;
	private String tweetMessageTemplate;
	private I18nHelper i18nHelper;

	public TweetDefinitionImpl(Issue issue, 
			ApplicationUser remoteUser, 
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

	public ApplicationUser getRemoteUser() {
		return this.remoteUser;
	}

	public String getTweetMessageTemplate() {
		return tweetMessageTemplate;
	}

	public void setTweetMessageTemplate(String tweetMessagetemplate) {
		this.tweetMessageTemplate = tweetMessagetemplate;
	}
}