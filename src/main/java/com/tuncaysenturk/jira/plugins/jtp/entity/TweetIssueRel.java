package com.tuncaysenturk.jira.plugins.jtp.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Indexed;

@Preload
public interface TweetIssueRel extends Entity {
	Long getIssueId();
	void setIssueId(Long issueId);
	
	@Indexed
	Long getTweetStatusId();
	@Indexed
	void setTweetStatusId(Long statusId);
}
