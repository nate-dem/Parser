package com.ef.exception;

public class CommandLineArgsParseException extends Exception  {
	
	private static final long serialVersionUID = 1L;
	
	public CommandLineArgsParseException() {
		super();
	}
	
	public CommandLineArgsParseException(String msg) {
		super(msg);
	}

}