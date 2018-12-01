package com.ef.service;


import java.util.ArrayList;
import java.util.List;

import com.ef.exception.InvalidWebLogFileException;
import com.ef.exception.WebLogFileNotFoundException;
import com.ef.model.CommandLineArgs;
import com.ef.observer.ConsoleLogger;
import com.ef.observer.Observer;

public interface ParserService {
	
	
	
	public void saveLogs(String pathToFile) throws InvalidWebLogFileException;

	public boolean filterFile(CommandLineArgs commandLineArg) throws WebLogFileNotFoundException;

	public boolean filterByIP(String ipAddress);
	
	public boolean retrieveLogs(CommandLineArgs commandLineArgs);
	

	
	public void notifyObservers();
	
}
