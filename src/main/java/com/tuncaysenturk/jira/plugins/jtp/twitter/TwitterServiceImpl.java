package com.tuncaysenturk.jira.plugins.jtp.twitter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.atlassian.jira.issue.Issue;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.TweetDefinition;
import com.tuncaysenturk.jira.plugins.jtp.persist.TweetIssueRelService;
import com.tuncaysenturk.jira.plugins.jtp.util.ExceptionMessagesUtil;

public class TwitterServiceImpl implements TwitterService {

	private static transient Logger logger = Logger.getLogger(TwitterServiceImpl.class);
	private final TweetIssueRelService tweetIssueRelService;
	private String consumerKey;
	private String consumerSecret;
	private String accessToken; 
	private String accessSecret; 
	
	public TwitterServiceImpl(TweetIssueRelService tweetIssueRelService) {
		this.tweetIssueRelService = tweetIssueRelService;
	}
	
	@Override
	public boolean tweet(TweetDefinition definition, String message) {
        try {
        	if (StringUtils.isEmpty(consumerKey) || StringUtils.isEmpty(consumerSecret)
        			|| StringUtils.isEmpty(accessSecret) || StringUtils.isEmpty(accessToken))
        		throw new IllegalArgumentException(JTPConstants.LOG_PRE + "Twitter auth tokens are not defined!");
            TwitterFactory factory = new TwitterFactory();
            Twitter twitter = factory.getInstance();
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
            twitter.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
            Status status = twitter.updateStatus(message);
            ExceptionMessagesUtil.cleanAllExceptionMessages();
            logger.info("status id:" + status.getId());
            associateStatusIdWithIssue(definition.getIssueObject(), status.getId());
            return true; 
        } catch (Exception te) {
            logger.error(JTPConstants.LOG_PRE + "Failed to get timeline: ", te);
            ExceptionMessagesUtil.addExceptionMessage("Failed to get timeline: ",  te);
            return false;
        } 
	}
	
	protected void associateStatusIdWithIssue(Issue issue, long statusId) {
		tweetIssueRelService.persistRelation(issue.getId(), statusId);
	}

	@Override
	public void setAccessTokens(String consumerKey, String consumerSecret,
			String accessToken, String accessSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessSecret = accessSecret;
		this.accessToken = accessToken;
	}

}
