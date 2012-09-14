package com.tuncaysenturk.jira.plugins.license;

public class LicenseStatus {

	private boolean valid;
	private String message;
	
	public LicenseStatus(boolean valid, String message) {
		this.valid = valid;
		this.message = message;
	}
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
