package com.ef.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleLogger implements Observer<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleLogger.class);
	
	@Override
	public void update(String msg) {
		LOGGER.info(msg);
	}

}
