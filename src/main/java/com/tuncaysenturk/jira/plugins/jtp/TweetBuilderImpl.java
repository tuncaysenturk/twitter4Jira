package com.tuncaysenturk.jira.plugins.jtp;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.tuncaysenturk.jira.plugins.jtp.twitter.TwitterService;

public class TweetBuilderImpl implements TweetBuilder {
	private static transient Logger logger = Logger
			.getLogger(TweetBuilderImpl.class);
	
	private static final int MAX_TWEET_CHARS = 140;
	private ApplicationProperties applicationProperties;
	private VelocityManager velocityManager;
	private TwitterService twitterService;

	public TweetBuilderImpl(
			CommentManager commentManager,
			ApplicationProperties applicationProperties,
			RendererManager rendererManager, 
			WatcherManager watcherManager,
			VelocityManager velocityManager, 
			TwitterService twitterService) {
		this.applicationProperties = applicationProperties;
		this.velocityManager = velocityManager;
		this.twitterService = twitterService;
	}

	public void buildAndSendTweet(TweetDefinition def)
			throws Exception {
		String tweetMessageTeplate = def.getTweetMessageTemplate();
		String baseUrl = this.applicationProperties.getString("jira.baseurl");
		
		Map<String, Object> contextParams = new HashMap<String, Object>();
		contextParams.put("baseurl", baseUrl);
	    contextParams.put("issue", def.getIssueObject());
	    contextParams.put("newline", "\n");
	    
		String tweetMessage = velocityManager.getEncodedBodyForContent(tweetMessageTeplate, baseUrl, contextParams);
		
		logger.info(JTPConstants.LOG_PRE + "Tweet is ready : " + tweetMessage);
		
		setAccessTokens();
		boolean tweetSucceeded = twitterService.tweet(def, trimTweet(tweetMessage));
		logger.info(JTPConstants.LOG_PRE + ((tweetSucceeded) ? "Tweet sent " : "Tweet could not be sent"));

	}
	
	private String trimTweet(String tweet) {
		if (StringUtils.isEmpty(tweet))
			return StringUtils.EMPTY;
		else if (MAX_TWEET_CHARS < tweet.length())
			return StringUtils.substring(tweet, 0, MAX_TWEET_CHARS);
		else
			return tweet;
	}
	
	private void setAccessTokens() {
		PropertySet propGet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
		String consumerKey = propGet.getString("consumerKey");
		String consumerSecret = propGet.getString("consumerSecret");
		String accessToken = propGet.getString("accessToken");
		String accessSecret = propGet.getString("accessSecret");
		twitterService.setAccessTokens(consumerKey, consumerSecret, accessToken, accessSecret);
	}

}