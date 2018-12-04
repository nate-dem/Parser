package com.ef.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class H2QueryHelper implements DbQueryHelper {

	private static final Map<String, String> queries = new HashMap<>();
	
	private static final String INSERT_LOG_ENTRY = "INSERT INTO PARSER.LOG_ENTRIES "
			+ "(START_DATE, IP_ADDRESS, REQUEST_METHOD, STATUS, USER_AGENT) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	private static final String SELECT_BLOCKED_IP = "SELECT IP_ADDRESS, COUNT(IP_ADDRESS) "
			+ "FROM PARSER.LOG_ENTRIES "
			+ "WHERE START_DATE >= ? AND START_DATE < ? "
			+ "GROUP BY IP_ADDRESS HAVING COUNT(IP_ADDRESS) >= ?";
	
	private static final String INSERT_BLOCKED_IP = "INSERT INTO PARSER.BLOCKED_IPS (IP_ADDRESS, NUM_REQUEST, REASON) "
			+ "VALUES (?, ?, ?)";
	
	static {
		queries.put("INSERT_LOG_ENTRY", INSERT_LOG_ENTRY);
		queries.put("SELECT_BLOCKED_IP", SELECT_BLOCKED_IP);
		queries.put("INSERT_BLOCKED_IP", INSERT_BLOCKED_IP);
	}

	@Override
	public String getQuery(String key) {
		return queries.get(key);
	}
	
}
