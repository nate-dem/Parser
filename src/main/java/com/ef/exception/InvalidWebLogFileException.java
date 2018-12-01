package com.ef.exception;

public class InvalidWebLogFileException extends Exception  {
	
		private static final long serialVersionUID = 1L;
		
		public InvalidWebLogFileException() {
			super();
		}
		
		public InvalidWebLogFileException(String msg) {
			super(msg);
		}

}
