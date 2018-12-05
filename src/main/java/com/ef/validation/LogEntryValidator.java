package com.ef.validation;

import org.apache.commons.validator.routines.InetAddressValidator;

public class LogEntryValidator {

	public static boolean validate(String[] entryStr){
		
		// log entry should have 5 elements: Date, IP, Request, Status, User Agent 
		if(entryStr.length != 5)
			return false;
		
 		// validate IP address 
		InetAddressValidator inetValidator = InetAddressValidator.getInstance();
		if (null == entryStr[1]  || !inetValidator.isValid(entryStr[1] ) )
			return false;
		
		return true;
	}
	
}
