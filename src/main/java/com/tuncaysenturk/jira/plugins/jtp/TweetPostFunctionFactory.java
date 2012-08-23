package com.tuncaysenturk.jira.plugins.jtp;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.tuncaysenturk.jira.plugins.jtp.twitter.JiraTwitterStream;

public class TweetPostFunctionFactory extends AbstractWorkflowPluginFactory
		implements WorkflowPluginFunctionFactory {
	private WebResourceManager webResourceManager;
	private ApplicationProperties applicationProperties;
	private JiraTwitterStream twitterStream;

	public TweetPostFunctionFactory(
			ApplicationProperties applicationProperties,
			WebResourceManager webResourceManager, 
			Authorizer authorizer,
			JiraTwitterStream twitterStream) {
		this.webResourceManager = webResourceManager;
		this.applicationProperties = applicationProperties;
		this.twitterStream = twitterStream;
		
		if (!this.twitterStream.isListening())
			this.twitterStream.streamUser();
	}

	@Override
	protected void getVelocityParamsForEdit(Map<String, Object> velocityParams,
			AbstractDescriptor descriptor) {

		velocityParams.put("emailIssueTo",
				getDescriptorParam(descriptor, "emailIssueTo", ""));
		velocityParams.put("tweetMessageTemplate",
				getDescriptorParam(descriptor, "tweetMessageTemplate", ""));

		getVelocityParamsForInput(velocityParams);
		VelocityHelper vh = (VelocityHelper) velocityParams.get("action");
		vh.setEmailIssueTo((String) velocityParams.get("emailIssueTo"));
		vh.setTweetMessageTemplate((String) velocityParams
				.get("tweetMessageTemplate"));
		velocityParams.put("action", vh);
	}

	@Override
	protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
		VelocityHelper vh = new VelocityHelper();
		vh.setTweetMessageTemplate((String) velocityParams
				.get("tweetMessageTemplate"));
		vh.setI18n(((JiraAuthenticationContext) ComponentManager
				.getComponentInstanceOfType(JiraAuthenticationContext.class))
				.getI18nHelper());

		velocityParams.put("action", vh);
		String baseUrl = this.applicationProperties.getString("jira.baseurl");
		velocityParams.put("baseUrl", baseUrl);

		this.webResourceManager
				.requireResource("jira.webresources:jira-global");
		this.webResourceManager
				.requireResource("jira.webresources:autocomplete");
	}

	@Override
	protected void getVelocityParamsForView(Map<String, Object> velocityParams,
			AbstractDescriptor descriptor) {
		getVelocityParamsForEdit(velocityParams, descriptor);
	}

	@Override
	public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("tweetMessageTemplate",
				extractSingleParam(formParams, "tweetMessageTemplate", ""));

		return params;
	}

	private String getDescriptorParam(AbstractDescriptor descriptor,
			String key, String defaultValue) {
		if (!(descriptor instanceof FunctionDescriptor)) {
			throw new IllegalArgumentException(
					"Descriptor must be a FunctionDescriptor.");
		}
		FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
		String value = (String) functionDescriptor.getArgs().get(key);
		if (value != null && value.trim().length() > 0)
			return value;
		else
			return defaultValue;
	}

	protected String extractSingleParam(Map<String, Object> params, String key,
			String defaultValue) {
		if ((params != null) && (params.containsKey(key))) {
			return super.extractSingleParam(params, key);
		}
		return defaultValue;
	}

	public String toString(String[] values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (sb.length() > 0)
				sb.append(",");
			sb.append(values[i]);
		}
		return sb.toString();
	}

}