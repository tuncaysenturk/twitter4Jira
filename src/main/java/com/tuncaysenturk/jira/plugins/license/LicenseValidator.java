package com.tuncaysenturk.jira.plugins.license;

public interface LicenseValidator {

	LicenseStatus getLicenseStatus();
	boolean isValid();
}
