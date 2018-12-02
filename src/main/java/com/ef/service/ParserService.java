package com.ef.service;

import java.util.List;

import com.ef.exception.InvalidWebLogFileException;
import com.ef.exception.WebLogFileNotFoundException;
import com.ef.model.CommandLineArgs;
import com.ef.model.IPRequest;

public interface ParserService {
	
	public void saveLogs(String pathToFile) throws InvalidWebLogFileException;

	public boolean filterFile(CommandLineArgs commandLineArg) throws WebLogFileNotFoundException;

	public boolean findByIP(String ipAddress);
	
	public List<IPRequest> findIps(CommandLineArgs commandLineArgs);
	
	public void notifyObservers();
	
	public boolean saveBlockedIps(List<IPRequest> iPRequests);
	
}
