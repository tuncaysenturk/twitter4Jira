package com.tuncaysenturk.jira.plugins.jtp.twitter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.issue.JiraTwitterIssueService;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public final class JiraTwitterStreamImpl extends StatusAdapter implements JiraTwitterStream{

	private static transient Logger logger = Logger.getLogger(JiraTwitterStreamImpl.class);
	private String consumerKey;
	private String consumerSecret;
	private String accessToken; 
	private String accessSecret;
	private JiraTwitterIssueService issueService;
	private ThirdPartyPluginLicenseStorageManager licenseStorageManager;
	private boolean isListening = false;
	static final JiraTwitterUserStreamListener listener = new JiraTwitterUserStreamListener();
	private static TwitterStream twitterStream;
	
	public JiraTwitterStreamImpl(JiraTwitterIssueService issueService,
			ThirdPartyPluginLicenseStorageManager licenseStorageManager) {
		this.issueService = issueService;
		this.licenseStorageManager = licenseStorageManager;
		listener.setJiraTwitterIssueService(this.issueService);
		listener.setLicenseStorageManager(licenseStorageManager);
	}
	
	public void streamUser() {
		if (!LicenseValidator.isValid(licenseStorageManager))
			logger.error(JTPConstants.LOG_PRE + "Invalid license");
		else if (isListening) {
			logger.error(JTPConstants.LOG_PRE + "This is another attempt to stream twitter account that has already been streaming by another thread");
		} else {
			twitterStream = new TwitterStreamFactory().getInstance();
	        try {
	        	listener.setJiraTwitterStream(twitterStream);
	        	listener.setJiraTwitterIssueService(issueService);
	        	listener.setLicenseStorageManager(licenseStorageManager);
	        	if (null == twitterStream.getConfiguration().getOAuthConsumerKey() || 
						null == twitterStream.getConfiguration().getOAuthConsumerSecret())
					twitterStream.setOAuthConsumer(consumerKey, consumerSecret);
	        	try {
	        		if (null == twitterStream.getOAuthAccessToken())
	        			twitterStream.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
				} catch (IllegalStateException e) {
					twitterStream.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
				}
				listener.setScreenName(twitterStream.getScreenName());
			} catch (Exception e) {
				logger.error(JTPConstants.LOG_PRE + "Error while streaming", e);
			}
			if (!isListening) {
				twitterStream.addListener(listener);
				twitterStream.user();
		        synchronized(this) {
		        	isListening = true;
		        }
			}
	        logger.info(JTPConstants.LOG_PRE + "Successfully streaming twitter account");
        }
	}
	
    @Override
	public void setAccessTokens(String consumerKey, String consumerSecret,
			String accessToken, String accessSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessSecret = accessSecret;
		this.accessToken = accessToken;
	}

	@Override
	public boolean isListening() {
		return isListening;
	}
	
	@Override
	public void setListening(boolean isListening) {
		synchronized(this) {
			this.isListening = isListening;
		}
	}

	@Override
	public boolean isValidAccessToken() {
		return !StringUtils.isEmpty(accessToken) && 
		!StringUtils.isEmpty(accessSecret) &&
		!StringUtils.isEmpty(consumerKey) &&
		!StringUtils.isEmpty(consumerSecret);
	}
}