package com.ef.exception;

public class InvalidLogFileException extends Exception  {
	
		private static final long serialVersionUID = 1L;
			
		public InvalidLogFileException(String msg) {
			super(msg);
		}

}
