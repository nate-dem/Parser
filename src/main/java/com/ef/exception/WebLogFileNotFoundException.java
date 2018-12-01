package com.ef.exception;

public class WebLogFileNotFoundException extends Exception  {
	
		private static final long serialVersionUID = 1L;
		
		public WebLogFileNotFoundException() {
			super();
		}
		
		public WebLogFileNotFoundException(String msg) {
			super(msg);
		}

}
