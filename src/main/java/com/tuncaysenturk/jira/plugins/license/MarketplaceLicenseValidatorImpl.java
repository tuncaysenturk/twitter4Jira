package com.tuncaysenturk.jira.plugins.license;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;

public class MarketplaceLicenseValidatorImpl extends AbstractLicenseValidator {
	private static final Logger logger = Logger
			.getLogger(MarketplaceLicenseValidatorImpl.class);

	public MarketplaceLicenseValidatorImpl(
			ThirdPartyPluginLicenseStorageManager licenseManager,
			ApplicationProperties applicationProperties,
			JiraLicenseService jiraLicenseService) {
		super(licenseManager, applicationProperties, jiraLicenseService);
	}
	
	private LicenseStatus getLicenseStatusForMarketPlace() {
		try {
			if (licenseManager.getLicense().isDefined()) {
				PluginLicense pluginLicense = licenseManager.getLicense().get();
				// Check and see if the stored license has an error. If not, it
				// is currently valid.
				if (!pluginLicense.getError().isDefined()) {
					// A license is currently stored and it is valid.
					return new LicenseStatus(true, JTPConstants.VALID_LICENSE);
				} else {
					if (JTPConstants.EXPIRED_LICENSE.equals(pluginLicense.getError().get()
							.name()))
						if (pluginLicense.isEvaluation()) {
							String message = JTPConstants.LOG_PRE + pluginLicense.getError().get().name();
							logger.error(message);
							return new LicenseStatus(false, message);
						}
						// else if (isSupportedVersion())
						// return new LicenseStatus(true,
						// pluginLicense.getError().get().name());
						else
							return new LicenseStatus(true, pluginLicense
									.getError().get().name());
					else {
						// A license is currently stored, however, it is invalid
						// (e.g. user count mismatch)
						String message = JTPConstants.LOG_PRE + pluginLicense.getError().get().name();
						logger.error(message);
						return new LicenseStatus(false, message);
					}
				}
			} else {
				// No license (valid or invalid) is stored.
				// Plugin should not work at all
				logger.error(JTPConstants.LOG_PRE + JTPConstants.NO_LICENSE);
				return new LicenseStatus(false, JTPConstants.NO_LICENSE);
			}
		} catch (PluginLicenseStoragePluginUnresolvedException e1) {
			return new LicenseStatus(false, JTPConstants.NO_STORAGE_PLUGIN_INSTALLED);
		}
	}
	
	public LicenseStatus getLicenseStatus() {
		// free version
		if (isSupportedVersion())
			return new LicenseStatus(true, JTPConstants.VALID_LICENSE);
		else
			return new LicenseStatus(false, JTPConstants.EXPIRED_LICENSE);
		// paid version
		// return getLicenseStatusForMarketPlace();
	}
}
