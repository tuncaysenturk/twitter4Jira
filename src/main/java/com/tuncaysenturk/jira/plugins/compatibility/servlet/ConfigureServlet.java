package com.tuncaysenturk.jira.plugins.compatibility.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.jtp.twitter.JiraTwitterStream;
import com.tuncaysenturk.jira.plugins.license.LicenseValidator;

public class ConfigureServlet extends HttpServlet {
	private static final long serialVersionUID = -4320175922941414956L;

	private static transient Logger logger = Logger.getLogger(ConfigureServlet.class);
	private static final String TEMPLATE = "templates/configure.vm";
	private final ApplicationProperties applicationProperties;
	private final LoginUriProvider loginUriProvider;
	private final UserManager userManager;
	private final TemplateRenderer renderer;
	private final I18nResolver i18nResolver;
	private final ProjectManager projectManager;
	private final ConstantsManager constantsManager;
	private final ThirdPartyPluginLicenseStorageManager licenseStorageManager;
	private JiraTwitterStream twitterStream;

	public ConfigureServlet(ApplicationProperties applicationProperties,
			LoginUriProvider loginUriProvider, 
			UserManager userManager,
			I18nResolver i18nResolver, 
			TemplateRenderer renderer, 
			ProjectManager projectManager,
			ConstantsManager constantsManager,
			JiraTwitterStream twitterStream,
			ThirdPartyPluginLicenseStorageManager licenseStorageManager) {
		this.applicationProperties = applicationProperties;
		this.loginUriProvider = loginUriProvider;
		this.userManager = userManager;
		this.i18nResolver = i18nResolver;
		this.renderer = renderer;
		this.projectManager = projectManager;
		this.constantsManager = constantsManager;
		this.twitterStream = twitterStream;
		this.licenseStorageManager = licenseStorageManager;
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
		
		renderer.render(TEMPLATE, context, resp.getWriter());
	}

	private Map<String, Object> initVelocityContext(HttpServletResponse resp) {
		resp.setContentType("text/html;charset=utf-8");
		URI servletConfigure = URI.create(applicationProperties.getBaseUrl() + JTPConstants.URL_CONFIGURE);
		URI servletConfigureTwitter = URI.create(applicationProperties.getBaseUrl() + JTPConstants.URL_CONFIGURE_TWITTER);

		List<String> errorMessages = new ArrayList<String>();
		PropertySet propSet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
		final Map<String, Object> context = new HashMap<String, Object>();
		context.put("baseUrl", URI.create(applicationProperties.getBaseUrl()));
		context.put("servletConfigure", servletConfigure);
		context.put("servletConfigureTwitter", servletConfigureTwitter);
		
		boolean licenseValid = LicenseValidator.isValid(licenseStorageManager);
		context.put("licenseValid", licenseValid);
		if (!licenseValid)
			errorMessages.add(i18nResolver.getText("jtp.configuration.license.invalid"));
		
		context.put("projects", getProjects());
		context.put("issueTypes", getIssueTypes());
		context.put("userId2", propSet.getString("userId"));
		context.put("projectId2", propSet.getString("projectId"));
		context.put("issueTypeId2", propSet.getString("issueTypeId"));
		context.put("isStopStreamingRequest", propSet.getBoolean("stopTweeting"));
		context.put("onlyFollowers2", propSet.getBoolean("onlyFollowers"));
		context.put("errorMessages", errorMessages);
		if (null == twitterStream.getTwitterScreenName())
			errorMessages.add(i18nResolver.getText("jtp.configuration.twitter.noTwitterAccountLoggedIn"));
		else
			context.put("twitterScreenName", twitterStream.getTwitterScreenName());
		context.put("licenseServletUrl", JTPConstants.URL_LICENSE);

		return context;
	}
	
	private Collection<Project> getProjects() {
		return projectManager.getProjectObjects();
	}
	
	private Collection<IssueType> getIssueTypes() {
		return constantsManager.getAllIssueTypeObjects();
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
		logger.info(JTPConstants.LOG_PRE + "setting are being changed");

		PropertySet propSet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
		propSet.setString("projectId", req.getParameter("projectId"));
		propSet.setString("userId", req.getParameter("userId"));
		propSet.setString("issueTypeId", req.getParameter("issueTypeId"));
		propSet.setBoolean("onlyFollowers", "on".equalsIgnoreCase(req.getParameter("onlyFollowers")));

		final Map<String, Object> context = initVelocityContext(resp);
		@SuppressWarnings("unchecked")
		List<String> errorMessages = (List<String>)context.get("errorMessages");
		if (userManager.getUserProfile(req.getParameter("userId")) == null)
			errorMessages.add(i18nResolver.getText("jtp.configuration.userid.invalid"));
		context.put("errorMessages", errorMessages);
		if (projectManager.getProjectObj(Long.parseLong(req.getParameter("projectId"))) == null)
			errorMessages.add(i18nResolver.getText("jtp.configuration.projectId.invalid"));
		
		renderer.render(TEMPLATE, context, resp.getWriter());

	}
	
	private void redirectToLogin(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.sendRedirect(loginUriProvider.getLoginUri(
				URI.create(req.getRequestURL().toString())).toASCIIString());
	}
}
