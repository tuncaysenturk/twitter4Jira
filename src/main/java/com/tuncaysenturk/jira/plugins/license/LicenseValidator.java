package com.tuncaysenturk.jira.plugins.license;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.opensymphony.module.propertyset.PropertySet;

public class LicenseValidator {

	private static final String VALID_LICENSE = "Valid License";
	private static final String EXPIRED_LICENSE = "EXPIRED";
	private static final String NO_LICENSE = "No Valid License Installed";
	private static final String NO_STORAGE_PLUGIN_INSTALLED = "Required resources failure. Please speak to a system administrator.";
	
	public static LicenseStatus getLicenseStatus(ThirdPartyPluginLicenseStorageManager licenseManager) {
		try {
			if (licenseManager.getLicense().isDefined())
			{
			    PluginLicense pluginLicense = licenseManager.getLicense().get();
			    //Check and see if the stored license has an error. If not, it is currently valid.
			    if (!pluginLicense.getError().isDefined()) {
			    	// A license is currently stored and it is valid.
			    	return new LicenseStatus(true, VALID_LICENSE);
			    } else {
			    	if (EXPIRED_LICENSE.equals(pluginLicense.getError().get().name()))
			    		return new LicenseStatus(true, pluginLicense.getError().get().name());
			    	else // A license is currently stored, however, it is invalid (e.g. user count mismatch)
			    		return new LicenseStatus(false, pluginLicense.getError().get().name());
			    }
			} else {
				// No license (valid or invalid) is stored.
				// Plugin should not work at all
				return new LicenseStatus(false, NO_LICENSE);
			}
		} catch (PluginLicenseStoragePluginUnresolvedException e1) {
			return new LicenseStatus(false, NO_STORAGE_PLUGIN_INSTALLED);
		}
	}

	public static boolean isValid(ThirdPartyPluginLicenseStorageManager licenseManager) {
		LicenseStatus licenseStatus = getLicenseStatus(licenseManager);
		
		PropertySet propSet = ComponentManager.getComponent(PropertiesManager.class).getPropertySet();
		
		propSet.setString("licenseMessage", licenseStatus.getMessage());
		
		return licenseStatus.isValid();
	}
}
