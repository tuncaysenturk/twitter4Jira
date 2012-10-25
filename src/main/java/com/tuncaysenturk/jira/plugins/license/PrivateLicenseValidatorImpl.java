package com.tuncaysenturk.jira.plugins.license;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;

public class PrivateLicenseValidatorImpl extends AbstractLicenseValidator {
	private static final Logger logger = Logger
			.getLogger(PrivateLicenseValidatorImpl.class);

	private PrivateLicense privateLicense;
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static boolean privateLicenseInitialized = false;

	private static final byte[] DIGEST_FOR_NON_MARKETPLACE_LICENSING = new byte[] {
			(byte) 0xEB, (byte) 0x8F, (byte) 0x1E, (byte) 0x41, (byte) 0x19,
			(byte) 0x72, (byte) 0xB2, (byte) 0xAE, (byte) 0xF5, (byte) 0x07,
			(byte) 0x65, (byte) 0xFE, (byte) 0x99, (byte) 0xAE, (byte) 0x8E,
			(byte) 0x3B, (byte) 0xA6, (byte) 0x55, (byte) 0xFA, (byte) 0x82,
			(byte) 0x05, (byte) 0x1C, (byte) 0x73, (byte) 0x81, (byte) 0xB0,
			(byte) 0x48, (byte) 0x31, (byte) 0x12, (byte) 0x10, (byte) 0xDD,
			(byte) 0x48, (byte) 0xB9, };


	private void initializePrivateLicense() throws Exception {
		if (!privateLicenseInitialized) {
			privateLicense.loadKeyRingFromResource("pubring.gpg",
					DIGEST_FOR_NON_MARKETPLACE_LICENSING);
			privateLicense.setLicenseEncodedFromResource(JTPConstants.LICENSE_FILENAME);
			privateLicenseInitialized = true;
		}
	}
	
	public PrivateLicenseValidatorImpl(ThirdPartyPluginLicenseStorageManager licenseManager,
			ApplicationProperties applicationProperties,
			JiraLicenseService jiraLicenseService) {
		super(licenseManager, applicationProperties, jiraLicenseService);
		this.privateLicense = new PrivateLicense();
	}

	
	public LicenseStatus getLicenseStatus() {
		return getLicenseStatusForNonMarketPlace();
	}
	
	private LicenseStatus getLicenseStatusForNonMarketPlace() {
		try {
			initializePrivateLicense();
		} catch (Exception e) {
			logger.error(JTPConstants.LOG_PRE + "exception caught while initializing private license", e);
			return new LicenseStatus(false, JTPConstants.NO_LICENSE);
		}
		if (privateLicense.isDefined()) {
			String jiraServerId = jiraLicenseService.getServerId();
			// -1 unlimited
			int jiraMaximumNumberOfUsers = jiraLicenseService.getLicense()
					.getMaximumNumberOfUsers();
			
			int licensedMaximumNumberOfUsers = Integer.parseInt(privateLicense.getFeature("maximumNumberOfUsers"));
			Date validUntil = null;
			try {
				validUntil = dateFormatter.parse(privateLicense.getFeature("validUntil"));
			} catch (ParseException e) {
				logger.error(JTPConstants.LOG_PRE+ "Invalid date format", e);
			}
			String licensedServerId = privateLicense.getFeature("serverId");
			if (!jiraServerId.equals(licensedServerId)) {
				logger.warn(JTPConstants.LOG_PRE + "serverId mismatch. " +
						"Expected/found serverId: " + licensedServerId + "/" + jiraServerId);
				return new LicenseStatus(false, JTPConstants.SERVER_ID_MISMATCH);
			} else if (licensedMaximumNumberOfUsers != -1 || 
					jiraMaximumNumberOfUsers > licensedMaximumNumberOfUsers) {
				logger.warn(JTPConstants.LOG_PRE + "macimum number of users mismatch. " +
						"Expected/found : " + licensedMaximumNumberOfUsers + "/" + jiraMaximumNumberOfUsers);
				return new LicenseStatus(false, JTPConstants.NUMBER_OF_USER_MISMATCH);
			} else if (validUntil.before(new Date())) {
				logger.warn(JTPConstants.LOG_PRE + "license expired. Renew your license for support and updates.");
				return new LicenseStatus(true, JTPConstants.EXPIRED_LICENSE);
			} else 
				return new LicenseStatus(true, JTPConstants.VALID_LICENSE);
		} else {
			// No license (valid or invalid) is stored.
			// Plugin should not work at all
			return new LicenseStatus(false, JTPConstants.NO_LICENSE);
		}
	}
	
	public void resetInitializer() {
		privateLicenseInitialized = false;
	}

	public boolean isValid() {
		privateLicenseInitialized = false;
		return super.isValid();
	}
}
