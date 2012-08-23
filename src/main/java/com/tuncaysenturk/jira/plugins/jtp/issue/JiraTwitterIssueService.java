package com.tuncaysenturk.jira.plugins.jtp.issue;


public interface JiraTwitterIssueService {

	public abstract void createIssue(String userName, String screenName, 
			String text, long projectId, String issueTypeId);
	
	/**
	 * adds comment to issue
	 * @param userName default user that will add comment to user
	 * @param screenName twitter username that replies to main issue tweet. 
	 * This will be added to comment as "this comment is added by @username"
	 * @param issueId issue id as long
	 * @param commentString comment string
	 */
	public abstract void addComment(String userName, String screenName, long issueId, String commentString);
}
