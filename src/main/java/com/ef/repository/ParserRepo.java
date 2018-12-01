package com.ef.repository;

import java.util.Date;
import java.util.stream.Stream;

import com.ef.util.DurationType;

public interface ParserRepo {
	
	public void saveRequestData(Stream<String> stream);
	
	// fetch
	
	// filter by ip

	public boolean filterRequestData(Date parsedStartDate, Date parsedEndDate, DurationType duration, int threshold);

	public boolean filterByIP(String ipAddress);
	
}
