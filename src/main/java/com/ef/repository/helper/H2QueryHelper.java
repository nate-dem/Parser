package com.ef.repository.helper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class H2QueryHelper implements DbQueryHelper {

	private static final Map<String, String> queries = new HashMap<>();

	private static final String INSERT_LOG_ENTRY = "INSERT INTO PARSER.LOG_ENTRIES "
			+ "(REQUEST_DATE, IP_ADDRESS, REQUEST_METHOD, STATUS, USER_AGENT) "
			+ "VALUES (?, ?, ?, ?, ?)";

	private static final String SELECT_BLOCKED_IP = "SELECT IP_ADDRESS IP, COUNT(IP_ADDRESS) IP_COUNT "
			+ "FROM PARSER.LOG_ENTRIES "
			+ "WHERE REQUEST_DATE BETWEEN ? AND ? "
			+ "GROUP BY IP_ADDRESS "
			+ "HAVING IP_COUNT >= ?";

	private static final String INSERT_BLOCKED_IP = "INSERT INTO PARSER.BLOCKED_IPS (IP_ADDRESS, NUM_REQUEST, REASON) "
			+ "VALUES (?, ?, ?)";
	
	private static final String SELECT_BLOCK_REASON = "SELECT * FROM PARSER.BLOCK_REASON "
			+ "WHERE START_DATE = ? AND DURATION = ? AND THRESHOLD = ? ";
		
	private static final String INSERT_BLOCK_REASON = "INSERT INTO PARSER.BLOCK_REASON (START_DATE, DURATION, THRESHOLD) "
			+ "VALUES (?, ?, ?)";

	static {
		queries.put("INSERT_LOG_ENTRY", INSERT_LOG_ENTRY);
		queries.put("SELECT_BLOCKED_IP", SELECT_BLOCKED_IP);
		queries.put("INSERT_BLOCKED_IP", INSERT_BLOCKED_IP);
		queries.put("INSERT_BLOCK_REASON", INSERT_BLOCK_REASON);
		queries.put("SELECT_BLOCK_REASON", SELECT_BLOCK_REASON);
	}

	@Override
	public String getQuery(String key) {
		return queries.get(key);
	}

}
