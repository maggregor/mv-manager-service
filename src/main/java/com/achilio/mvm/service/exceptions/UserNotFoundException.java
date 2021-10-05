package com.achilio.mvm.service.exceptions;

public class UserNotFoundException extends Exception{

	public UserNotFoundException(String errorMsg) {
		super(errorMsg);
	}
}
