package com.ef.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleLogger implements Observer<String> {

	private static final Logger logger = LoggerFactory.getLogger(ConsoleLogger.class);

	@Override
	public void update(String msg) {
		logger.info(msg);
	}

}
