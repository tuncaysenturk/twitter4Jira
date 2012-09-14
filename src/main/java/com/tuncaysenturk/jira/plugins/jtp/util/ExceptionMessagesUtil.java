package com.tuncaysenturk.jira.plugins.jtp.util;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ExceptionMessagesUtil {
	private static Set<String> exceptionMessages = new HashSet<String>();
	
	private static final String UNKNOWN_HOST = "Twitter stream error, check your internet connection"; 
	private static final String STREAM_CLOSED = "Stream closed.";
	private static final String INVALID_LICENSE = "Your license is invalid. Please check your license.";
	
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
	
	public static void cleanAllExceptionMessages() {
		exceptionMessages = new HashSet<String>();
	}
	
	public static void cleanInternetRelatedExceptionMessages() {
		exceptionMessages.remove(STREAM_CLOSED);
		exceptionMessages.remove(UNKNOWN_HOST);
	}
}
