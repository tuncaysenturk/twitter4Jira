package com.tuncaysenturk.jira.plugins.jtp;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

public abstract interface TweetDefinition {
	public static final String FORMAT_HTML = "html";
	public static final String FORMAT_TEXT = "text";

	public abstract Issue getIssueObject();

	public abstract String getTweetMessageTemplate();

	public abstract I18nHelper getI18n();

	public abstract ApplicationUser getRemoteUser();
}