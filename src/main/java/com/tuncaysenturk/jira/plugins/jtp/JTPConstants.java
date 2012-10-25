package com.tuncaysenturk.jira.plugins.jtp;

public class JTPConstants {
	public static final String LOG_PRE = "[Twitter Plugin for JIRA] ";
	public static final String URL_CONFIGURE = "/plugins/servlet/jtp/configure";
	public static final String URL_CONFIGURE_TWITTER = "/plugins/servlet/jtp/configure/twitter";
	public static final String URL_LICENSE = "/plugins/servlet/com.tuncaysenturk.jira.plugins.plugin-license-compatibility/license";
	
	// License Constants
	public static String LICENSE_FILENAME = "jep-license.out";
	public static final String VALID_LICENSE = "Valid License";
	public static final String EXPIRED_LICENSE = "EXPIRED";
	public static final String NO_LICENSE = "No Valid License Installed";
	public static final String NO_STORAGE_PLUGIN_INSTALLED = "Required resources failure. Please speak to a system administrator.";
	public static final String SERVER_ID_MISMATCH = "Your JIRA server id mismatches with your JIRA Enhancer Plugin license";
	public static final String NUMBER_OF_USER_MISMATCH = "Maximum Number of Users for JIRA license mismatches with your JIRA Enhancer Plugin license";
	// End of license constants
}
