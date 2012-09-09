package com.tuncaysenturk.jira.plugins.jtp.persist;

import net.java.ao.Query;

import org.apache.commons.lang.ArrayUtils;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.tuncaysenturk.jira.plugins.jtp.entity.TweetIssueRel;

public class TweetIssueRelServiceImpl implements TweetIssueRelService {

	private final ActiveObjects ao;
	
	public TweetIssueRelServiceImpl(ActiveObjects ao) {
		this.ao = ao;
	}
	
	@Override
	public void persistRelation(final Long issueId, final Long tweetStatusId) {
        final TweetIssueRel tweetIssueRel = ao.create(TweetIssueRel.class);
        tweetIssueRel.setIssueId(issueId);
        tweetIssueRel.setTweetStatusId(tweetStatusId);
        tweetIssueRel.save();
	}

	@Override
	public TweetIssueRel findTweetIssueRelByTweetStatusId(final Long tweetStatusId) {
    	TweetIssueRel[] arr = ao.find(TweetIssueRel.class, Query.select().where("TWEET_STATUS_ID = ?", tweetStatusId));
    	if (!ArrayUtils.isEmpty(arr))
    		return arr[0];
		return null;
	}
}
