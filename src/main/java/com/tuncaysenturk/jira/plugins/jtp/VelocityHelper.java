package com.tuncaysenturk.jira.plugins.jtp;

import com.atlassian.jira.util.I18nHelper;

public class VelocityHelper
{
  private I18nHelper i18n;
  private String emailIssueTo;
  private String tweetMessageTemplate;

  public VelocityHelper()
  {
  }

  public I18nHelper getI18n() {
    return this.i18n;
  }

  public String getTweetMessageTemplate() {
	return tweetMessageTemplate;
}

public void setTweetMessageTemplate(String tweetMessageTemplate) {
	this.tweetMessageTemplate = tweetMessageTemplate;
}

public void setI18n(I18nHelper i18n) {
    this.i18n = i18n;
  }

  public String getEmailIssueTo() {
    return this.emailIssueTo;
  }

  public void setEmailIssueTo(String emailIssueTo) {
    this.emailIssueTo = emailIssueTo;
  }

}