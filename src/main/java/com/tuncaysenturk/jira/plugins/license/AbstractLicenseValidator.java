package com.tuncaysenturk.jira.plugins.license;

import org.apache.log4j.Logger;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;

public abstract class AbstractLicenseValidator implements LicenseValidator {
	private static final Logger logger = Logger
			.getLogger(AbstractLicenseValidator.class);

	protected final ThirdPartyPluginLicenseStorageManager licenseManager;
	protected final ApplicationProperties applicationProperties;
	// Atlassian Marketplace does not support some countries
	// To handle licenses within those countries I need to get JIRA server id
	protected final JiraLicenseService jiraLicenseService;
	
	protected static final String LAST_SUPPORTED_JIRA_VERSION = "5.1.7";

	public AbstractLicenseValidator(
			ThirdPartyPluginLicenseStorageManager licenseManager,
			ApplicationProperties applicationProperties,
			JiraLicenseService jiraLicenseService) {
		this.licenseManager = licenseManager;
		this.applicationProperties = applicationProperties;
		this.jiraLicenseService = jiraLicenseService;
	}
	

	/**
	 * This method will check last supported version
	 * 
	 * @param existingVersion
	 *            e.g. "5.1.4" will be converted to an array of three
	 *            <tt>String</tt>s<br>
	 *            Then <tt>LAST_SUPPORTED_JIRA_VERSION</tt> and this converted
	 *            array will be compared
	 * @return
	 */
	public boolean isSupportedVersion() {
		return isSupportedVersion(applicationProperties.getVersion(),
				LAST_SUPPORTED_JIRA_VERSION);
	}

	public boolean isSupportedVersion(String existingVersion,
			String supportedVersion) {
		logger.info(JTPConstants.LOG_PRE + "checking supported version ...");
		if (StringUtils.isEmpty(existingVersion)
				|| StringUtils.isEmpty(supportedVersion)) {
			String message = JTPConstants.LOG_PRE + "version details has to be given, existingVersion : "
				+ existingVersion + ", supportedVersion : "
				+ supportedVersion;
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		if (StringUtils.countMatches(existingVersion, ".") < 1) {
			String message = JTPConstants.LOG_PRE + "existingVersion has to be in x.y or x.y.z format";
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		if (StringUtils.countMatches(supportedVersion, ".") < 1) {
			String message = JTPConstants.LOG_PRE + "supportedVersion has to be in x.y or x.y.z format";
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		String[] existingVersionArray = existingVersion.split("\\.");
		String[] supportedVersionArray = supportedVersion.split("\\.");

		for (int i = 0; i < 2; i++) {
			int s = Integer.parseInt(supportedVersionArray[i]);
			int e = Integer.parseInt(existingVersionArray[i]);
			if (s < e)
				return false;
			else if (s > e)
				return true;
		}
		if (existingVersionArray.length > 2
				&& Integer.parseInt(supportedVersionArray[2]) < Integer
						.parseInt(existingVersionArray[2])) {
			logger.error(JTPConstants.LOG_PRE + "wrong version info");
			return false;
		}
		logger.info(JTPConstants.LOG_PRE + "supported version check passed successfully ...");
		return true;
	}

	public abstract LicenseStatus getLicenseStatus(); 

	public boolean isValid() {
		return getLicenseStatus().isValid();
	}
}
