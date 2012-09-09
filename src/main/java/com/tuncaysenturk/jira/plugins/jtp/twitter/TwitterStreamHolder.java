package com.tuncaysenturk.jira.plugins.jtp.twitter;

import java.util.List;

public interface TwitterStreamHolder {

	public abstract List<JiraTwitterStream> getAllTwitterStreams();
	public abstract void removeAllTwitterStreams();
	public abstract void addTwitterStream(JiraTwitterStream twitterStream);
	public abstract void removeTwitterStream(JiraTwitterStream stream);
}
