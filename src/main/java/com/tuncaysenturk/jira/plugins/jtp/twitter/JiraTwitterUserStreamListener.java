package com.tuncaysenturk.jira.plugins.jtp.twitter;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.Twitter4JHelper;
import com.tuncaysenturk.jira.plugins.jtp.issue.JiraTwitterIssueService;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public class JiraTwitterUserStreamListener implements UserStreamListener {
	private static transient Logger logger = Logger
			.getLogger(JiraTwitterUserStreamListener.class);
	private JiraTwitterIssueService issueService;
	private TwitterStream twitterStream;
	private String screenName;
	private ThirdPartyPluginLicenseStorageManager licenseStorageManager;
	private Twitter twitter;
	private Long lastDateForQueryingFollowers;
	private Set<Long> followers;
	private static Long REQUERY_FOLLOWERS_TIMEOUT = 1000L * 60 * 30; // 30 mins to requery followers

	public void setLicenseStorageManager(
			ThirdPartyPluginLicenseStorageManager licenseStorageManager) {
		this.licenseStorageManager = licenseStorageManager;
	}

	public void setJiraTwitterIssueService(JiraTwitterIssueService issueService) {
		this.issueService = issueService;
	}

	public void setJiraTwitterStream(TwitterStream twitterStream) {
		this.twitterStream = twitterStream;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getScreenName() {
		return this.screenName;
	}

	/**
	 * When Jira os plugin starts, there has to be internet connection Otherwise
	 * screen name will be null, so that it will mess. We have to set screen
	 * name again if it's null
	 */
	public void checkAndSetScreenName() {
		if (null == screenName)
			try {
				setScreenName(twitterStream.getScreenName());
			} catch (Exception e) {
				logger.error(JTPConstants.LOG_PRE
						+ "Error catching screen name", e);
			}
	}

	private boolean isStopTweetingRequestArrived(PropertySet propSet) {
		return propSet.getBoolean("stopTweeting");
	}

	private void initializeTwitter(String consumerKey,
			String consumerSecret,
			String accessToken,
			String accessSecret) throws TwitterException {
		twitter = Twitter4JHelper.initializeTwitter(consumerKey, consumerSecret, accessToken, accessSecret);
	}

	private boolean isFollower(long userId) {
		try {
			if (null == twitter) {
				PropertySet propSet = ComponentManager.getComponent(
						PropertiesManager.class).getPropertySet();
				initializeTwitter(propSet.getString("consumerKey"),
						propSet.getString("consumerSecret"),
						propSet.getString("accessToken"),
						propSet.getString("accessSecret"));
			}
			if(null == followers || null == lastDateForQueryingFollowers || 
					System.currentTimeMillis() - lastDateForQueryingFollowers > REQUERY_FOLLOWERS_TIMEOUT) {
				followers = Twitter4JHelper.toSet(twitter.getFollowersIDs(-1L));
				lastDateForQueryingFollowers = System.currentTimeMillis();
			}

			return followers.contains(userId);
		} catch (TwitterException e) {
			logger.error(JTPConstants.LOG_PRE
					+ "Error while looking for followers, twitter user id:"
					+ userId, e);
			return false;
		}
	}

	@Override
	public void onStatus(Status status) {
		PropertySet propSet = ComponentManager.getComponent(
				PropertiesManager.class).getPropertySet();
		if (!LicenseValidator.isValid(licenseStorageManager))
			logger.error(JTPConstants.LOG_PRE + "Invalid license");
		else if (isStopTweetingRequestArrived(propSet))
			logger.warn(JTPConstants.LOG_PRE + "There is a stop request, so I'll do nothing");
		else {
			logger.info(JTPConstants.LOG_PRE + "onStatus @"
					+ status.getUser().getScreenName() + " - "
					+ status.getText());
			checkAndSetScreenName();
			if (StringUtils.isEmpty(screenName)
					|| status.getUser().getScreenName().equals(screenName))
				return;
			String text = StringUtils.trim(status.getText().replace(
					"@" + screenName, ""));

			if (propSet.getBoolean("onlyFollowers") && !isFollower(status.getUser().getId())) {
				logger.warn(JTPConstants.LOG_PRE + status.getUser().getScreenName() + " is not a follower but mentioned to create issue");
				return;
			}
			String userName = propSet.getString("userId");
			String issueTypeId = propSet.getString("issueTypeId");
			if (status.getInReplyToStatusId() == -1) {
				// -1 means that this is not reply, it's main tweet
				String projectId = propSet.getString("projectId");
				issueService.createIssue(userName, status.getUser()
						.getScreenName(), text, Long.parseLong(projectId),
						issueTypeId);
			} else if (status.getInReplyToStatusId() > 0) {
				// this is a reply tweet, so add comment to main issue
				long statusId = status.getInReplyToStatusId();
				String issueId = propSet.getString("" + statusId);
				if (null != issueId) {
					issueService.addComment(userName, status.getUser()
							.getScreenName(), Long.parseLong(issueId), text);
					// assosiate this tweet with issue,
					// so that replies to this tweet will be comments to the issue
					propSet.setString("" + status.getId(), "" + issueId);
				}
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		logger.info(JTPConstants.LOG_PRE + "Got a status deletion notice id:"
				+ statusDeletionNotice.getStatusId());
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		logger.info(JTPConstants.LOG_PRE
				+ "Got a direct message deletion notice id:" + directMessageId);
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		logger.info(JTPConstants.LOG_PRE + "Got a track limitation notice:"
				+ numberOfLimitedStatuses);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		logger.info(JTPConstants.LOG_PRE + "Got scrub_geo event userId:"
				+ userId + " upToStatusId:" + upToStatusId);
	}

	@Override
	public void onFriendList(long[] friendIds) {
		// do nothing
	}

	public void onFavorite(User source, User target, Status favoritedStatus) {
		// do nothing
	}

	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		// do nothing
	}

	public void onFollow(User source, User followedUser) {
		logger.info(JTPConstants.LOG_PRE + "just logging, onFollow source:@"
				+ source.getScreenName() + " target:@"
				+ followedUser.getScreenName());
		if (null != followers)
			followers.add(followedUser.getId());
	}

	public void onRetweet(User source, User target, Status retweetedStatus) {
		logger.info(JTPConstants.LOG_PRE + "just logging, onRetweet @"
				+ retweetedStatus.getUser().getScreenName() + " - "
				+ retweetedStatus.getText());
	}

	public void onDirectMessage(DirectMessage directMessage) {
		logger.info("just logging, onDirectMessage text:"
				+ directMessage.getText());
	}

	public void onUserListMemberAddition(User addedMember, User listOwner,
			UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListMemberAddition added member:@"
				+ addedMember.getScreenName() + " listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListMemberDeletion(User deletedMember, User listOwner,
			UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListMemberDeleted deleted member:@"
				+ deletedMember.getScreenName() + " listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListSubscription(User subscriber, User listOwner,
			UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListSubscribed subscriber:@"
				+ subscriber.getScreenName() + " listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListUnsubscription(User subscriber, User listOwner,
			UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListUnsubscribed subscriber:@"
				+ subscriber.getScreenName() + " listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListCreation(User listOwner, UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just loggimng, onUserListCreated  listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListUpdate(User listOwner, UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListUpdated  listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserListDeletion(User listOwner, UserList list) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserListDestroyed  listOwner:@"
				+ listOwner.getScreenName() + " list:" + list.getName());
	}

	public void onUserProfileUpdate(User updatedUser) {
		logger.info(JTPConstants.LOG_PRE
				+ "just logging, onUserProfileUpdated user:@"
				+ updatedUser.getScreenName());
	}

	public void onBlock(User source, User blockedUser) {
		logger.info(JTPConstants.LOG_PRE + "just logging, onBlock source:@"
				+ source.getScreenName() + " target:@"
				+ blockedUser.getScreenName());
	}

	public void onUnblock(User source, User unblockedUser) {
		logger.info(JTPConstants.LOG_PRE + "just logging, onUnblock source:@"
				+ source.getScreenName() + " target:@"
				+ unblockedUser.getScreenName());
	}

	public void onException(Exception ex) {
		logger.error(JTPConstants.LOG_PRE + "onException:" + ex.getMessage(),
				ex);
	}

}
