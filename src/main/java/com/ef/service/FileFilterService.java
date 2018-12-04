package com.ef.service;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.CommandLineArgs;

public interface FileFilterService {
	public boolean filterFile(CommandLineArgs commandLineArg) throws InvalidLogFileException;
	
	public boolean findByIP(String ipAddress);
}
