package com.tuncaysenturk.jira.plugins.jtp.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface TweetIssueRel extends Entity {
	Long getIssueId();
	void setIssueId(Long issueId);
	Long getTweetStatusId();
	void setTweetStatusId(Long statusId);
}
