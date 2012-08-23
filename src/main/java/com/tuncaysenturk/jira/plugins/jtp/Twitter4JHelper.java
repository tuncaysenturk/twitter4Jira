package com.tuncaysenturk.jira.plugins.jtp;

import java.util.HashSet;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class Twitter4JHelper {

	public static HashSet<Long> toSet(IDs ids) {
		if (ids == null) {
			return null;
		}

		long[] temp = ids.getIDs();

		HashSet<Long> result = new HashSet<Long>(temp.length);

		for (int i = 0; i < temp.length; i++) {
			result.add(temp[i]);
		}

		return result;
	}
	
	public static Twitter initializeTwitter(String consumerKey, String consumerSecret,
			String token, String tokenSecret) throws TwitterException {
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
		return twitter;
	}
}
