package com.tuncaysenturk.jira.plugins.jtp.util;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ExceptionMessagesUtil {
	private static Set<String> exceptionMessages = new HashSet<String>();
	
	private static final String UNKNOWN_HOST = "Twitter stream error, check your internet connection"; 
	private static final String STREAM_CLOSED = "Stream closed.";
	private static final String INVALID_LICENSE = "Your license is invalid. Please check your license.";
	private static final String READ_TIMED_OUT = "Read timed out";
	private static final String ACCESS_TOKENS_NOT_SET = "Access tokens are not set. Please set parameters in " +
						"Administration > Plugins > Jira Twitter Plugin Configure section";
	public static final String AUTHENTICATION_CREDENTIAL_FAILURE_CODE = "401";
	
	public static Set<String> getExceptionMessages() {
		return exceptionMessages;
	}
	
	private static boolean addException(Exception e) {
		boolean added = true;
		if (null != e.getCause() && e.getCause() instanceof UnknownHostException)
			exceptionMessages.add(UNKNOWN_HOST);
		else
			added = false;
		return added;
	}
	
	public static void addExceptionMessage(String message, Exception e) {
		if (!addException(e))
			exceptionMessages.add(message);
	}
	
	public static void addExceptionMessage(String message) {
		exceptionMessages.add(message);
	}
	
	public static void addExceptionMessage(Exception e) {
		if (!addException(e))
			exceptionMessages.add(e.getMessage());
	}
	
	public static void addLicenseExceptionMessage() {
		addExceptionMessage(INVALID_LICENSE);
	}
	
	public static void addAccessTokenNotSetExceptionMessage() {
		addExceptionMessage(ACCESS_TOKENS_NOT_SET);
	}
	
	public static void cleanAllExceptionMessages() {
		exceptionMessages = new HashSet<String>();
	}
	
	public static void cleanInternetRelatedExceptionMessages() {
		exceptionMessages.remove(STREAM_CLOSED);
		exceptionMessages.remove(UNKNOWN_HOST);
		exceptionMessages.remove(READ_TIMED_OUT);
		exceptionMessages.remove(ACCESS_TOKENS_NOT_SET);
		for(String excMsg:exceptionMessages) {
			if (excMsg.startsWith(AUTHENTICATION_CREDENTIAL_FAILURE_CODE))
				exceptionMessages.remove(excMsg);
		}
	}
	
	public static void cleanLicenseExceptionMessage() {
		exceptionMessages.remove(INVALID_LICENSE);
	}
}
