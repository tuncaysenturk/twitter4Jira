package com.tuncaysenturk.jira.plugins.license;

import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;

public class LicenseValidator {

	public static boolean isValid(ThirdPartyPluginLicenseStorageManager licenseManager) {
//		return true;
		try {
			if (licenseManager.getLicense().isDefined())
			{
			    PluginLicense pluginLicense = licenseManager.getLicense().get();
			    //Check and see if the stored license has an error. If not, it is currently valid.
			    if (!pluginLicense.getError().isDefined()) {
			    	return true;
			    } else
			    	return false;
			} else {
				return false;
			}
		} catch (PluginLicenseStoragePluginUnresolvedException e1) {
	    	return false;
		}
	}
}
