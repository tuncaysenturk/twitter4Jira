package com.tuncaysenturk.jira.plugins.jtp.twitter;

public interface JiraTwitterStream {

	public abstract void streamUser();
	public abstract void setAccessTokens(String consumerKey, String consumerSecret,
			String accessToken, String accessSecret);
	public abstract boolean isListening();
	public void setListening(boolean isListening);
	public abstract boolean isValidAccessToken();
}
