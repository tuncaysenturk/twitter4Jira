package com.tuncaysenturk.jira.plugins.jtp.twitter;

import java.util.ArrayList;
import java.util.List;

public class TwitterStreamHolderImpl implements TwitterStreamHolder{

	List<JiraTwitterStream> twitterStreams = new ArrayList<JiraTwitterStream>();
	@Override
	public List<JiraTwitterStream> getAllTwitterStreams() {
		return twitterStreams;
	}

	@Override
	public void removeAllTwitterStreams() {
		for(JiraTwitterStream stream: twitterStreams) {
			stream.stopListener();
			stream = null;
		}
		twitterStreams = new ArrayList<JiraTwitterStream>();
	}

	@Override
	public void addTwitterStream(JiraTwitterStream twitterStream) {
		twitterStreams.add(twitterStream);
	}

	@Override
	public void removeTwitterStream(JiraTwitterStream stream) {
		JiraTwitterStream targetStream = null;
		for(JiraTwitterStream eachStream: twitterStreams) {
			if(eachStream.equals(stream)) {
				targetStream = eachStream;
				break;
			}
		}
		if (null != targetStream) {
			targetStream.stopListener();
			twitterStreams.remove(targetStream);
			targetStream = null;
		}
		
	}

}
