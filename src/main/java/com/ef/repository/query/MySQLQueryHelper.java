package com.ef.repository.query;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class MySQLQueryHelper implements DbQueryHelper {
	
	private static final Map<String, String> queries = new HashMap<>();

	private static final String INSERT_LOG_ENTRY = "INSERT IGNORE INTO PARSER.LOG_ENTRIES "
			+ "(START_DATE, IP_ADDRESS, REQUEST_METHOD, STATUS, USER_AGENT) "
			+ "VALUES (?, INET_ATON(?), ?, ?, ?)";
	
	private static final String SELECT_BLOCKED_IP = "SELECT INET_NTOA(IP_ADDRESS) AS IP_ADDRESS, COUNT(IP_ADDRESS) "
			+ "FROM PARSER.LOG_ENTRIES "
			+ "WHERE START_DATE >= ? AND START_DATE < ? "
			+ "GROUP BY IP_ADDRESS HAVING COUNT(IP_ADDRESS) >= ?";
	
	private static final String INSERT_BLOCKED_IP = "INSERT IGNORE INTO PARSER.BLOCKED_IPS (IP_ADDRESS, NUM_REQUEST, REASON) "
			+ "VALUES (INET_ATON(?), ?, ?)";
	
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