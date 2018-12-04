package com.ef.exception;

public class DBOperationException extends Exception  {
	
		private static final long serialVersionUID = 1L;
		
		public DBOperationException(String msg) {
			super(msg);
		}

}
