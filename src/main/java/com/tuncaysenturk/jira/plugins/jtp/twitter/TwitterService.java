package com.tuncaysenturk.jira.plugins.jtp.twitter;

import com.tuncaysenturk.jira.plugins.jtp.TweetDefinition;

public interface TwitterService {
	public void setAccessTokens(String consumerKey, String consumerSecret,
			String accessToken, String accessSecret);
	public boolean tweet(TweetDefinition definition, String message);
}
