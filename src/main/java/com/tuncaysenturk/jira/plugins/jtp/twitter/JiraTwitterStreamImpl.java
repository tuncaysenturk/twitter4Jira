package com.tuncaysenturk.jira.plugins.jtp.twitter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.StatusAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.issue.JiraTwitterIssueService;
import com.tuncaysenturk.jira.plugins.jtp.persist.TweetIssueRelService;
import com.tuncaysenturk.jira.plugins.jtp.util.ExceptionMessagesUtil;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public final class JiraTwitterStreamImpl extends StatusAdapter implements JiraTwitterStream{

	private static transient Logger logger = Logger.getLogger(JiraTwitterStreamImpl.class);
	private final TweetIssueRelService tweetIssueRelService;
	private JiraTwitterIssueService issueService;
	private final LicenseValidator licenseValidator;
	private JiraTwitterUserStreamListener listener;
	private TwitterStream twitterStream;
	ApplicationProperties propSet;
	
	public JiraTwitterStreamImpl(JiraTwitterIssueService issueService,
			LicenseValidator licenseValidator,
			TweetIssueRelService tweetIssueRelService) {
		this.issueService = issueService;
		this.licenseValidator = licenseValidator;
		this.tweetIssueRelService = tweetIssueRelService;
		propSet = ComponentAccessor.getApplicationProperties();
	}
	
	public void startListener() {
		if (!licenseValidator.isValid()) {
			logger.error(JTPConstants.LOG_PRE + "License problem, see configuration page");
			ExceptionMessagesUtil.addLicenseExceptionMessage();
		} else if (null != listener) {
			// it should not come here
			// first it has to be stopped  
			logger.warn(JTPConstants.LOG_PRE + "This is another attempt to stream twitter account that has already been streaming by another thread." +
					"It has to be stopped before.");
		} else {
			twitterStream = new TwitterStreamFactory().getInstance();
			
	        try {
	        	listener = new JiraTwitterUserStreamListener();
	        	listener.setJiraTwitterStream(twitterStream);
	        	listener.setJiraTwitterIssueService(issueService);
	        	listener.setLicenseValidator(licenseValidator);
	        	listener.setTweetIssueRelService(tweetIssueRelService);
	        	if (null == twitterStream.getConfiguration().getOAuthConsumerKey() || 
						null == twitterStream.getConfiguration().getOAuthConsumerSecret())
					twitterStream.setOAuthConsumer(propSet.getString("consumerKey"), propSet.getString("consumerSecret"));
	        	try {
        			AccessToken accessToken = new AccessToken(propSet.getString("accessToken"), propSet.getString("accessTokenSecret"));
        			twitterStream.setOAuthAccessToken(accessToken);
				} catch (IllegalStateException e) {
					logger.error(JTPConstants.LOG_PRE + "Exception while obtaining access tokens", e);
					ExceptionMessagesUtil.addExceptionMessage("Exception while obtaining access tokens : ", e);
				}
				
				twitterStream.addListener(listener);
				twitterStream.user();
		        listener.setScreenName(twitterStream.getScreenName());
		        logger.info(JTPConstants.LOG_PRE + "Successfully streaming twitter account");
			} catch (Exception e) {
				logger.error(JTPConstants.LOG_PRE + "Error while streaming", e);
				ExceptionMessagesUtil.addExceptionMessage("Error while streaming : ", e);
			}
        }
	}
	
	@Override
	public boolean isListening() {
		return (null != listener);
	}
	

	@Override
	public boolean isValidAccessToken() {
		return !StringUtils.isEmpty(propSet.getString("accessToken")) && 
		!StringUtils.isEmpty(propSet.getString("accessTokenSecret")) &&
		!StringUtils.isEmpty(propSet.getString("consumerKey")) &&
		!StringUtils.isEmpty(propSet.getString("consumerSecret")) &&
		!StringUtils.isEmpty(propSet.getString("accessTokenVerifier"));
	}

	@Override
	public String getTwitterScreenName() {
		if (null == twitterStream)
			return null;
		try {
			return twitterStream.getScreenName();
		} catch (IllegalStateException e) {
			logger.error(JTPConstants.LOG_PRE + "Error while getting screen name", e);
		} catch (TwitterException e) {
			logger.error(JTPConstants.LOG_PRE + "Error while getting screen name", e);
			ExceptionMessagesUtil.addExceptionMessage("Error while getting Twitter screen name : ", e);
		}
		return null;
	}
	
	public void stopListener() {
		if (null != twitterStream) {
	    	twitterStream.shutdown();
	    	twitterStream = null;
		}
		if (null != listener)
			listener.shutdown();
    	listener = null;
	}
	
	public boolean isAlive() {
		return (null != listener && listener.isAlive());
	}
}