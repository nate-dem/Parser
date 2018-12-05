package com.ef.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateForamtter {

	private DateForamtter() {
	}

	public static Date fromString(String date, String format) throws ParseException {

		try {
			return new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			throw e;
		}

	}
}
