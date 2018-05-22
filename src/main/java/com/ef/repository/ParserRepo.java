package com.ef.repository;

import java.util.Date;
import java.util.stream.Stream;

public interface ParserRepo {
	
	public void saveRequestData(Stream<String> stream);

	public boolean filterRequestData(Date parsedStartDate, Date parsedEndDate, String duration, int threshold);

	public boolean filterByIP(String ipAddress);
	
	public boolean filterAccessLogFile(String pathToFile, Date parsedStartDate, Date parsedEndDate, int threshold);
	
}
