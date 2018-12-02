package com.ef.repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import com.ef.model.CommandLineArgs;
import com.ef.model.IPRequest;

public interface ParserRepo {
	
	public void saveLogs(Stream<String> stream);
	
	public List<IPRequest> findIps(CommandLineArgs commandLineArgs, Date endDate);

	public boolean filterByIP(String ipAddress);
	
	public boolean saveBlockedIps(List<IPRequest> ipRequests);
	
}
