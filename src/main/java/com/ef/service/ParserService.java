package com.ef.service;

public interface ParserService {
	
	public void saveRequestData(String pathToFile);

	public boolean filterRequestData(String pathToFile, String startDate, String duration, String thresholdInp);

	public boolean filterByIP(String ipAddress);
	
}
