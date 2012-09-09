package com.tuncaysenturk.jira.plugins.jtp.twitter;

public interface JiraTwitterStream {

	public abstract void startListener();
	public abstract void stopListener();
	public abstract boolean isListening();
	public abstract boolean isValidAccessToken();
	public abstract String getTwitterScreenName();
	
}
