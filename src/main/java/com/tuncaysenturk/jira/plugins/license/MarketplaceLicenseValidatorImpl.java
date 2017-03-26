package com.tuncaysenturk.jira.plugins.license;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;

public class MarketplaceLicenseValidatorImpl extends AbstractLicenseValidator {

	public MarketplaceLicenseValidatorImpl(final PluginLicenseManager licenseManager,
			ApplicationProperties applicationProperties) {
		super(licenseManager, applicationProperties);
	}
	
	public LicenseStatus getLicenseStatus() {
		// JTP is free
		return new LicenseStatus(true, JTPConstants.VALID_LICENSE);
	}
}
