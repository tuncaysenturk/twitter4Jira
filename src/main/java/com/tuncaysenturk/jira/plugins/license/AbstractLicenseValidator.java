package com.tuncaysenturk.jira.plugins.license;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.api.license.PluginLicenseManager;

public abstract class AbstractLicenseValidator implements LicenseValidator {

	protected final PluginLicenseManager licenseManager;
	protected final ApplicationProperties applicationProperties;

	public AbstractLicenseValidator(final PluginLicenseManager licenseManager,
			ApplicationProperties applicationProperties) {
		this.licenseManager = licenseManager;
		this.applicationProperties = applicationProperties;
	}

	public abstract LicenseStatus getLicenseStatus();

	public boolean isValid() {
		return getLicenseStatus().isValid();
	}
}
