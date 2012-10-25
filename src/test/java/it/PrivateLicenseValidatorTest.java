package it;


import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.license.LicenseDetails;
import com.tuncaysenturk.jira.plugins.jtp.JTPConstants;
import com.tuncaysenturk.jira.plugins.license.LicenseStatus;
import com.tuncaysenturk.jira.plugins.license.PrivateLicenseValidatorImpl;



public class PrivateLicenseValidatorTest extends FuncTestCase {
	private static final String TEST_LICENSE_MAX_USERS_MISMATCH = "test-license-user-mismatch.out";
	private static final String TEST_LICENSE_SERVER_ID_MISMATCH = "test-license-serverId-mismatch.out";
	private static final String TEST_LICENSE_EXPIRED = "test-license-expired.out";
	private static final String TEST_LICENSE_VALID = "test-license-valid.out";
	
	
	private PrivateLicenseValidatorImpl licenseValidator;
	
	@Mock private JiraLicenseService jiraLicenseService;
	@Mock private LicenseDetails license;
	
	@Before
	public void setUpTest() {
		MockitoAnnotations.initMocks(this);
		licenseValidator = new PrivateLicenseValidatorImpl(null, null, jiraLicenseService);
		
		// This is needed by DoubleConverter getDisplayFormat
        when(jiraLicenseService.getServerId()).thenReturn("BP8Q-WXN6-SKX3-NB5M");
        when(jiraLicenseService.getLicense()).thenReturn(license);
        when(license.getMaximumNumberOfUsers()).thenReturn(-1);
	}

	@After
	public void tearDownTest() {
	}
	
	@Test()
	public void testShouldGiveUserMismatchErrorForNonMarketPlace() {
		JTPConstants.LICENSE_FILENAME = TEST_LICENSE_MAX_USERS_MISMATCH;
		LicenseStatus licenseStatus = licenseValidator.getLicenseStatus();
		assertFalse(licenseStatus.isValid());
		
		assertEquals(JTPConstants.NUMBER_OF_USER_MISMATCH, licenseStatus.getMessage());
	}
	
	@Test()
	public void testShouldGiveServerIdMismatchErrorForNonMarketPlace() {
		when(jiraLicenseService.getServerId()).thenReturn("ANOTHER-SERVER_LICENSE");
		licenseValidator.resetInitializer();
		JTPConstants.LICENSE_FILENAME = TEST_LICENSE_SERVER_ID_MISMATCH;
		LicenseStatus licenseStatus = licenseValidator.getLicenseStatus();
		assertFalse(licenseStatus.isValid());
		
		assertEquals(JTPConstants.SERVER_ID_MISMATCH, licenseStatus.getMessage() );
	}
	
	@Test()
	public void testShouldWarnAsExpiredForNonMarketPlace() {
		licenseValidator.resetInitializer();
		JTPConstants.LICENSE_FILENAME = TEST_LICENSE_EXPIRED;
		LicenseStatus licenseStatus = licenseValidator.getLicenseStatus();
		assertTrue(licenseStatus.isValid());
		
		assertEquals(JTPConstants.EXPIRED_LICENSE, licenseStatus.getMessage());
	}
	
	@Test()
	public void testShouldSuccedLicenseForNonMarketPlace() {
		licenseValidator.resetInitializer();
		JTPConstants.LICENSE_FILENAME = TEST_LICENSE_VALID;
		LicenseStatus licenseStatus = licenseValidator.getLicenseStatus();
		assertTrue(licenseStatus.isValid());
		
		assertEquals(JTPConstants.VALID_LICENSE, licenseStatus.getMessage());
	}
}
