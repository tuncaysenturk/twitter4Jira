package com.tuncaysenturk.jira.plugins.jtp;


public abstract interface TweetBuilder
{
  public abstract void buildAndSendTweet(TweetDefinition tweetDefinition)
    throws Exception;
}