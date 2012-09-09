package com.tuncaysenturk.jira.plugins.jtp.persist;

import com.atlassian.activeobjects.tx.Transactional;
import com.tuncaysenturk.jira.plugins.jtp.entity.TweetIssueRel;

@Transactional
public interface TweetIssueRelService {

	public void persistRelation(Long issueId, Long tweetStatusId);
	
	public TweetIssueRel findTweetIssueRelByTweetStatusId(final Long tweetStatusId);
}
