package com.tuncaysenturk.jira.plugins.license;


public interface LicenseValidator {

	
	/**
	 * This method will check last supported version
	 * @param existingVersion e.g. "5.1.4" will be converted to an array of three <tt>String</tt>s<br>
	 * Then <tt>LAST_SUPPORTED_JIRA_VERSION</tt> and this converted array will be compared 
	 * @return
	 */
	boolean isSupportedVersion();
	
	boolean isSupportedVersion(String existingVersion, String supportedVersion);
	
	LicenseStatus getLicenseStatus();

	boolean isValid();
}
