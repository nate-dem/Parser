package com.ef.util;

import java.text.SimpleDateFormat;

public class Helper {

	public SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	public SimpleDateFormat getFilterDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
	}
}
