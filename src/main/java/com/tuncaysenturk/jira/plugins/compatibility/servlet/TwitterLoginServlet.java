package com.tuncaysenturk.jira.plugins.compatibility.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.twitter.JiraTwitterStream;
import com.tuncaysenturk.jira.plugins.jtp.twitter.TwitterStreamHolder;
import com.tuncaysenturk.jira.plugins.jtp.util.ExceptionMessagesUtil;
import com.tuncaysenturk.jira.plugins.license.LicenseStatus;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public class TwitterLoginServlet extends HttpServlet {
	private static final long serialVersionUID = -4320175922941414956L;

	private static transient Logger logger = Logger
			.getLogger(TwitterLoginServlet.class);
	private static final String TEMPLATE = "templates/configure-twitter.vm";
	private static final String ACTION_LOGIN = "login";
	private static final String IS_CALLBACK = "isCallBack";
	private static final String ACTION_STOP = "stop";
	private static final String ACTION_RESTART = "restart";
	private static final String QUESTION_MARK = "?";
	private static final String EQUALS = "=";
	private static final String TRUE = "true";


	private final ApplicationProperties applicationProperties;
	private final LoginUriProvider loginUriProvider;
	private final UserManager userManager;
	private final TemplateRenderer renderer;
	private final I18nResolver i18nResolver;
	private final ThirdPartyPluginLicenseStorageManager licenseStorageManager;
	private JiraTwitterStream twitterStream;
	private TwitterStreamHolder twitterStreamHolder;

	public TwitterLoginServlet(ApplicationProperties applicationProperties,
			LoginUriProvider loginUriProvider, UserManager userManager,
			I18nResolver i18nResolver, TemplateRenderer renderer,
			JiraTwitterStream twitterStream,
			ThirdPartyPluginLicenseStorageManager licenseStorageManager,
			TwitterStreamHolder twitterStreamHolder) {
		this.applicationProperties = applicationProperties;
		this.loginUriProvider = loginUriProvider;
		this.userManager = userManager;
		this.i18nResolver = i18nResolver;
		this.renderer = renderer;
		this.twitterStream = twitterStream;
		this.licenseStorageManager = licenseStorageManager;
		this.twitterStreamHolder = twitterStreamHolder;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (userManager.getRemoteUsername() == null) {
			redirectToLogin(req, resp);
			return;
		} else if (!hasAdminPermission()) {
			handleUnpermittedUser(req, resp);
			return;
		}
		
		final Map<String, Object> context = initVelocityContext(resp);
		if (null != req.getParameter(IS_CALLBACK) && "true".equals(req.getParameter(IS_CALLBACK).toString()))
			twitterCallback(req, resp, context);
		else if (req.getParameter("action") != null) {
			if (ACTION_RESTART.equals(req.getParameter("action").toString()))
				restartListener(req, resp, context);
			else if ((ACTION_STOP.equals(req.getParameter("action").toString())))
				stopListener(req, resp, context, true);
		}

		renderer.render(TEMPLATE, context, resp.getWriter());
	}
	
	private void twitterCallback(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) {
		if (null == req.getParameter("denied") && null != req.getParameter("oauth_verifier")) {
			String verifier = req.getParameter("oauth_verifier").toString();
			
			PropertySet propSet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
			propSet.setString("accessTokenVerifier", verifier);
			
			Twitter twitter = new TwitterFactory().getInstance();
			 
	        twitter.setOAuthConsumer(propSet.getString("consumerKey"),propSet.getString("consumerSecret"));
	        RequestToken requestToken = new RequestToken(propSet.getString("requestToken"), propSet.getString("requestTokenSecret"));
	        
	        propSet.remove("requestToken");
			propSet.remove("requestTokenSecret");
			
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,verifier);
				propSet.setString("accessToken", accessToken.getToken());
				propSet.setString("accessTokenSecret", accessToken.getTokenSecret());
				User user = twitter.verifyCredentials();
				logger.info(JTPConstants.LOG_PRE + "User :" + user.getScreenName() + " logged in successfully");
				restartListener(req, resp, context);
			} catch (TwitterException e) {
				logger.error(JTPConstants.LOG_PRE + "Error while streaming", e);
				ExceptionMessagesUtil.addExceptionMessage("Error while streaming : ", e);
			}
		} else {
			logger.warn(JTPConstants.LOG_PRE + "User denied Twitter authorization, plugin will not work properly");
			ExceptionMessagesUtil.addExceptionMessage("User denied Twitter authorization, plugin will not work properly");
		}
       	
	}


	public void login(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) {
		Twitter twitter = new TwitterFactory().getInstance();

		try {
			PropertySet propSet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
			URI servletConfigureTwitter = URI.create(applicationProperties.getBaseUrl() + JTPConstants.URL_CONFIGURE_TWITTER);
			String callbackURL = servletConfigureTwitter + QUESTION_MARK + IS_CALLBACK +  EQUALS + TRUE;
			twitter.setOAuthConsumer(propSet.getString("consumerKey"),propSet.getString("consumerSecret"));
			RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL);

			String requestToken_token = requestToken.getToken();
			String requestToken_secret = requestToken.getTokenSecret();
			
			propSet.setString("requestToken", requestToken_token);
			propSet.setString("requestTokenSecret", requestToken_secret);
			
			String redirectUrl = requestToken.getAuthorizationURL();
			resp.sendRedirect(redirectUrl);
			
		} catch (TwitterException e) {
			logger.error(JTPConstants.LOG_PRE + "Exception while logging to Twitter", e);
			@SuppressWarnings("unchecked")
			List<String> errorMessages = (List<String>)context.get("errorMessages");
			errorMessages.add(e.getMessage());
		} catch (IOException e) {
			logger.error(JTPConstants.LOG_PRE + "Exception while logging to Twitter", e);
		}
	}

	private Map<String, Object> initVelocityContext(HttpServletResponse resp) {
		resp.setContentType("text/html;charset=utf-8");
		URI servletConfigure = URI.create(applicationProperties.getBaseUrl() + JTPConstants.URL_CONFIGURE);
		URI servletConfigureTwitter = URI.create(applicationProperties.getBaseUrl() + JTPConstants.URL_CONFIGURE_TWITTER);

		List<String> errorMessages = new ArrayList<String>();
		PropertySet propSet = ComponentManager.getComponent(
				PropertiesManager.class).getPropertySet();
		final Map<String, Object> context = new HashMap<String, Object>();
		context.put("baseUrl", URI.create(applicationProperties.getBaseUrl()));
		context.put("servletConfigure", servletConfigure);
		context.put("servletConfigureTwitter", servletConfigureTwitter);
		context.put("licenseServletUrl", JTPConstants.URL_LICENSE);
		context.put("accessToken", propSet.getString("accessToken"));
		context.put("accessTokenSecret", propSet.getString("accessTokenSecret"));
		LicenseStatus licenseStatus = LicenseValidator.getLicenseStatus(licenseStorageManager);
		context.put("licenseValid", licenseStatus.isValid());
		context.put("licenseMessage", propSet.getString("licenseMessage"));
		if (null != twitterStream && twitterStream.isAlive())
			ExceptionMessagesUtil.cleanInternetRelatedExceptionMessages();
		context.put("exceptionMessages", ExceptionMessagesUtil.getExceptionMessages());
		
		if (null == twitterStream.getTwitterScreenName())
			errorMessages.add(i18nResolver.getText("jtp.configuration.twitter.noTwitterAccountLoggedIn"));
		else
			context.put("twitterScreenName", twitterStream.getTwitterScreenName());
		context.put("listenerStatus", twitterStream.isListening());

		if (StringUtils.isEmpty(propSet.getString("consumerKey")))
			errorMessages.add(i18nResolver.getText("jtp.configuration.consumerKey.blank"));
		else
			context.put("consumerKey2", propSet.getString("consumerKey"));
		
		if (StringUtils.isEmpty(propSet.getString("consumerSecret")))
			errorMessages.add(i18nResolver.getText("jtp.configuration.consumerSecret.blank"));
		else
			context.put("consumerSecret2", propSet.getString("consumerSecret"));
		context.put("errorMessages", errorMessages);

		return context;
	}

	private void handleUnpermittedUser(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		final Map<String, Object> context = new HashMap<String, Object>();
		context.put("errorMessage", i18nResolver
				.getText("jtp.configuration.submit.caption.unpermitted"));
		renderer.render(TEMPLATE, context, resp.getWriter());
	}

	private boolean hasAdminPermission() {
		String user = userManager.getRemoteUsername();
		try {
			return user != null
					&& (userManager.isAdmin(user) || userManager
							.isSystemAdmin(user));
		} catch (NoSuchMethodError e) {
			return user != null && userManager.isSystemAdmin(user);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (userManager.getRemoteUsername() == null) {
			redirectToLogin(req, resp);
			return;
		} else if (!hasAdminPermission()) {
			handleUnpermittedUser(req, resp);
			return;
		}

		PropertySet propSet = ComponentManager.getComponent(
				PropertiesManager.class).getPropertySet();
		propSet.setString("consumerKey", req.getParameter("consumerKey"));
		propSet.setString("consumerSecret", req.getParameter("consumerSecret"));

		final Map<String, Object> context = initVelocityContext(resp);
		if (req.getParameter("action") != null) {
			if (ACTION_LOGIN.equals(req.getParameter("action")))
				login(req, resp, context);
		}
		
		renderer.render(TEMPLATE, context, resp.getWriter());
	}
	
	private void redirectToLogin(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.sendRedirect(loginUriProvider.getLoginUri(
				URI.create(req.getRequestURL().toString())).toASCIIString());
	}
	
	/**
	 * in configure.vm admin may check listener whether there is a problem, 
	 * and update tokens, if listenere does not work properly
	 * then admin may restart twitter listener from this link
	 * This method restarts twitter stream listener
	 * @throws IOException 
	 */
	private void restartListener(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context){
		logger.info(JTPConstants.LOG_PRE + "Trying to stream twitter account");
		
		if (!twitterStream.isValidAccessToken())
			logger.error(JTPConstants.LOG_PRE + "Access tokens are not set. Please set parameters from " +
					"Administration > Plugins > Jira Twitter Plugin Configure section");
		else {
			stopListener(req, resp, context, false);
			twitterStream.startListener();
			twitterStreamHolder.addTwitterStream(twitterStream);
			logger.info(JTPConstants.LOG_PRE + "Successfully listening twitter account for new issues");
			try {
				resp.sendRedirect(((URI) context.get("servletConfigureTwitter")).toString());
			} catch (IOException e) {
				logger.error(JTPConstants.LOG_PRE + "Sending redirect error", e);
			}
		}
	}
	
	private void stopListener(HttpServletRequest req, HttpServletResponse resp, 
			Map<String, Object> context, boolean redirect) {
		// all streams will be stopped via holder.
//		twitterStream.stopListener();
		twitterStreamHolder.removeAllTwitterStreams();
		logger.info(JTPConstants.LOG_PRE + "Stopped listening twitter account for new issues");
		try {
			if (redirect)
				resp.sendRedirect(((URI) context.get("servletConfigureTwitter")).toString());
		} catch (IOException e) {
			logger.error(JTPConstants.LOG_PRE + "Sending redirect error", e);
		}
	}


}
