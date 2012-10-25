package com.tuncaysenturk.jira.plugins.license;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class MarketplaceLicenseValidatorTest {

	private MarketplaceLicenseValidatorImpl licenseValidator;
	@Before
	public void setUp() throws Exception {
		licenseValidator = new MarketplaceLicenseValidatorImpl(null, null, null);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIsSupportedVersionWithInvalidSupportedVersion() {
		String supportedVersion = "There is no dot in supported version";
		String existingVersion = "5.1.4";
		
		licenseValidator.isSupportedVersion(existingVersion, supportedVersion);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIsSupportedVersionWithInvalidExistingVersion() {
		String supportedVersion = "5.1.4";
		String existingVersion = "This is invalid";
		
		licenseValidator.isSupportedVersion(existingVersion, supportedVersion);
	}
	
	@Test()
	public void testIsSupportedVersion() {
		String supportedVersion = "4.4.0";
		String existingVersion = "4.3.4";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "5.1.2";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.4.1";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.4.0";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.35.9";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.42.3";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.3";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.4";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.5";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.4";
		supportedVersion = "4.4";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.3";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.5";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.3.5";
		assertTrue(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
		
		existingVersion = "4.5.2";
		assertFalse(licenseValidator.isSupportedVersion(existingVersion, supportedVersion));
	}
}
