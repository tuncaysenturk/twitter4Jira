package com.tuncaysenturk.jira.plugins.jtp.issue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.message.I18nResolver;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
public class JiraTwitterIssueServiceImpl implements JiraTwitterIssueService {

	private static transient Logger logger = Logger.getLogger(JiraTwitterIssueServiceImpl.class);
	private IssueService issueService;
	private CommentService commentService;
	private JiraAuthenticationContext authenticationContext;
	private final UserManager userManager;
	private final I18nResolver i18nResolver;


	public JiraTwitterIssueServiceImpl(UserManager userManager, 
			I18nResolver i18nResolver,
			CommentService commentService) {
		issueService = ComponentManager.getInstance().getIssueService();
		authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
		this.userManager = userManager;
		this.i18nResolver = i18nResolver;
		this.commentService = commentService;
	}

	@Override
	public void createIssue(String userName, String screenName, 
			String text, long projectId, String issueTypeId) {
		logger.info(JTPConstants.LOG_PRE + "Creating Issue...");
		
		MutableIssue issue = createIssue(userName, projectId, screenName, text, issueTypeId);

		logger.info(JTPConstants.LOG_PRE + "Issue created :!" + issue.getKey());
	}
	
	public void addComment(String userName, String screenName, long issueId, String commentString) {
		User user = userManager.getUser(userName);
		authenticationContext.setLoggedInUser(user);
		Issue issue = issueService.getIssue(user, issueId).getIssue();
		String comment = commentString +  "\n" + i18nResolver.getText("jtp.comment-issue.footer", screenName);
		commentService.create(user, issue, comment, false, new SimpleErrorCollection());		
	}

	private MutableIssue createIssue(String userName, long projectId, 
			String screenName, String text, String issueTypeId) {
		User user = userManager.getUser(userName);
		IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
		issueInputParameters.setProjectId(projectId)
				.setIssueTypeId(issueTypeId)
				.setSummary(text)
				.setReporterId(userName)
				.setAssigneeId(userName)
				.setDescription(text +  "\n" + i18nResolver.getText("jtp.create-issue.footer", screenName))
				.setStatusId("1");

		authenticationContext.setLoggedInUser(user);
		CreateValidationResult createValidationResult = issueService.validateCreate(user, issueInputParameters);
		MutableIssue issue = create(user, createValidationResult);

		return issue;
	}

	public MutableIssue create(User user, CreateValidationResult createValidationResult) {
		MutableIssue issue = null;

		if (createValidationResult.isValid()) {
			IssueResult createResult = issueService.create(user, createValidationResult);
			if (createResult.isValid()) {
				issue = createResult.getIssue();
				logger.info(JTPConstants.LOG_PRE + "Created " + issue.getKey());
			} else {
				Collection<String> errorMessages = createResult.getErrorCollection().getErrorMessages();
				for (String errorMessage : errorMessages) {
					logger.info(JTPConstants.LOG_PRE + errorMessage);
				}
			}
		} else {
			Collection<String> errorMessages = createValidationResult.getErrorCollection().getErrorMessages();
			for (String errorMessage : errorMessages) {
				logger.info(JTPConstants.LOG_PRE + errorMessage);
			}
			Map<String, String> errors = createValidationResult.getErrorCollection().getErrors();
			Set<String> errorKeys = errors.keySet();
			for (String errorKey : errorKeys) {
				logger.info(JTPConstants.LOG_PRE + errors.get(errorKey));
			}
		}
		return issue;
	}
}
