package com.ef.repository;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ef.model.BlockedIp;
import com.ef.model.CommandLineArgs;
import com.ef.model.IPRequest;
import com.ef.model.ServerRequest;
import com.ef.util.DurationType;
import com.ef.util.ParserConstants;

import java.sql.PreparedStatement;

public class ParserRepoImpl implements ParserRepo {

	private final Logger logger = LoggerFactory.getLogger(ParserRepoImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	
	// mysql insert statement.  Avoids insertion of duplicate records based on start_date & ip_address value pairs.
	private static final String INSERT_REQUEST_LOG_SQL = "insert ignore into ServerRequestLogs "
			+ "(start_date, ip_address, request_method, status, user_agent) "
			+ "values (?, INET_ATON(?), ?, ?, ?)";
	
	private static final String SELECT_IP_BY_CRITERIA_SQL = "SELECT id, INET_NTOA(ip_address), COUNT(ip_address) "
			+ "FROM ServerRequestLogs "
			+ "WHERE start_date >= ? AND start_date <= ? GROUP BY ip_address HAVING COUNT(ip_address) >= ?  ";
	
	private static final String INSERT_BLOCKED_IP_SQL = "insert ignore into BlockedIps (ip_address, reason)"
			+ " values (INET_ATON(?), ?)"; 

	public ParserRepoImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void saveLogs(Stream<String> stream) {
		
		try {
			final AtomicInteger counter = new AtomicInteger();
			List<ServerRequest> requests = new ArrayList<>();
			long start = System.currentTimeMillis();
			stream.forEach(str -> {
				String[] trimInput = str.split(ParserConstants.LOG_FILE_DELIMITER);
				
				try {
					Date parsedDate = new SimpleDateFormat(ParserConstants.LOG_DATE_FORMAT).parse(trimInput[0]);

					ServerRequest serverRequest = new ServerRequest();
					serverRequest.setDate(parsedDate);
					serverRequest.setIp(trimInput[1]);
					serverRequest.setRequestMethod(trimInput[2]);
					serverRequest.setStatus(trimInput[3]);
					serverRequest.setUserAgent(trimInput[4]);
					requests.add(serverRequest);
					
					logger.info("processed " + counter.incrementAndGet());
					
					if(requests.size() % 25000 == 0) {
							executeInsertBatch(requests);
							requests.clear();
					}
					
					/*
					jdbcTemplate.update(INSERT_REQUEST_LOG_SQL, new Object[] { 
							new java.sql.Timestamp(serverRequest.getDate().getTime()),
							serverRequest.getIp(),
							serverRequest.getRequestMethod(),
							serverRequest.getStatus(),
							serverRequest.getUserAgent(),
						});*/
						
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			});
			executeInsertBatch(requests);
			requests.clear();
			logger.info(ParserConstants.DB_IMPORT_COMPLETED);
			logger.info("Time Taken = {} ms" , (System.currentTimeMillis()-start));
		} catch (NullPointerException e1) {
			logger.error(e1.getMessage());
			throw e1;
		}

	}
	
	@Override
	public List<IPRequest> findIps(CommandLineArgs commandLineArgs, Date endDate) {
		
		List<IPRequest> iPRequests = new ArrayList<>();

		try {
			
			logger.info(ParserConstants.PROCESSING_QUERY);
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_IP_BY_CRITERIA_SQL,
					new Object[] { commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold()  } );
			
			for (Map<String, Object> row : rows) {
				logger.info("Result from SELECT_IP_BY_CRITERIA_SQL {} ", row);
				IPRequest iPRequest = new IPRequest();
				iPRequest.setIp(row.get("INET_NTOA(ip_address)").toString());
				String numRequest = row.get("COUNT(ip_address)").toString();
				iPRequest.setNumberOfRequests(Integer.valueOf(numRequest));
				iPRequests.add(iPRequest);
			}
			
			return iPRequests;
			
		} catch (NullPointerException | DataAccessException e) {
			logger.error(e.getMessage());
			throw e;
		}
		
	}
	
	@Override
	public boolean saveBlockedIps(List<IPRequest> ipRequests) {
		
		 jdbcTemplate.batchUpdate(INSERT_BLOCKED_IP_SQL, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				IPRequest request = ipRequests.get(i);
				//ps.setObject(1, new java.sql.Timestamp(request.getDate().getTime()));
				ps.setString(1, request.getIp());
				String reason = "Made requests: " + request.getNumberOfRequests();
				ps.setString(2, reason );
			}

			@Override
			public int getBatchSize() {
				return ipRequests.size();
			}

		  });
		
		/*
		// insert query to log blocked requests. Avoids insertion of duplicate records based on date_time, ip_address and duration value pairs.
		INSERT_BLOCKED_IP_SQL
		//PreparedStatement preparedInsertStmt = (PreparedStatement) conn.prepareStatement(insertQuery);
		
		final AtomicInteger counter = new AtomicInteger();
		
		while (rs.next()) {

			String ipAddress = rs.getString("INET_NTOA(ip_address)");
			String timestamp = rs.getString("date_time");
			int numRequest = rs.getInt("COUNT(ip_address)");
			logger.info("IP Address: " + ipAddress + " | # of Requests: " + numRequest + " | Timestamp: " + timestamp);
			
			preparedInsertStmt.setObject(1, parsedStartDate);
			preparedInsertStmt.setString(2, ipAddress);
			preparedInsertStmt.setString(3, duration.name());
			preparedInsertStmt.setInt(4, numRequest);
			String remark = "Request Exceed " + threshold + " from " + parsedStartDate + " to " + parsedEndDate;
			preparedInsertStmt.setString(5, remark);
			preparedInsertStmt.addBatch();
			
			counter.incrementAndGet();
			if(counter.get() % 5000 == 0)
				preparedStmt.executeBatch();
		}
		
		preparedInsertStmt.executeBatch(); */
		return true;
	}

	@Override
	public boolean filterByIP(String ipAddress) {
		return false;
	}

	/*
	public boolean filterRequestData(Date parsedStartDate, Date parsedEndDate, DurationType duration, int threshold) {

		String selectSQL = "SELECT id, INET_NTOA(ip_address), date_time, COUNT(ip_address) FROM request_log WHERE date_time >= ? AND date_time <= ? GROUP BY ip_address HAVING COUNT(ip_address) >= ?  ";
		
		try(Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(selectSQL)) {
			
			preparedStmt.setObject(1, parsedStartDate);
			preparedStmt.setObject(2, parsedEndDate);
			preparedStmt.setInt(3, threshold);

			logger.info(ParserConstants.PROCESSING_QUERY);
			// execute select SQL statement
			ResultSet rs = preparedStmt.executeQuery();

			if (!rs.isBeforeFirst()) {
				logger.info(ParserConstants.NO_RESULT_FOUND);
				return false;
			}
			
			// insert query to log blocked requests. Avoids insertion of duplicate records based on date_time, ip_address and duration value pairs.
			String insertQuery = " insert ignore into request_search_log (date_time, ip_address, duration, num_requests, remark)"
					+ " values (?, INET_ATON(?), ?, ?, ?)"; 
			PreparedStatement preparedInsertStmt = (PreparedStatement) conn.prepareStatement(insertQuery);
			
			final AtomicInteger counter = new AtomicInteger();
			
			while (rs.next()) {

				String ipAddress = rs.getString("INET_NTOA(ip_address)");
				String timestamp = rs.getString("date_time");
				int numRequest = rs.getInt("COUNT(ip_address)");
				logger.info("IP Address: " + ipAddress + " | # of Requests: " + numRequest + " | Timestamp: " + timestamp);
				
				preparedInsertStmt.setObject(1, parsedStartDate);
				preparedInsertStmt.setString(2, ipAddress);
				preparedInsertStmt.setString(3, duration.name());
				preparedInsertStmt.setInt(4, numRequest);
				String remark = "Request Exceed " + threshold + " from " + parsedStartDate + " to " + parsedEndDate;
				preparedInsertStmt.setString(5, remark);
				preparedInsertStmt.addBatch();
				
				counter.incrementAndGet();
				if(counter.get() % 5000 == 0)
					preparedStmt.executeBatch();
			}
			
			preparedInsertStmt.executeBatch();
			
		} catch (NullPointerException | SQLException e) {
			logger.error(e.getMessage());
		}
		
		return true;

	}
	
	public boolean filterByIP(String ipAddress) {

		String selectSQL = "SELECT id, INET_NTOA(ip_address), date_time FROM request_log WHERE ip_address = INET_ATON(?) ";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(selectSQL)) {
			preparedStmt.setObject(1, ipAddress);
			// execute select SQL statement
			ResultSet rs = preparedStmt.executeQuery();
			logger.info(ParserConstants.PROCESSING_QUERY);
			
			if (!rs.isBeforeFirst()) {
				logger.info(ParserConstants.NO_RESULT_FOUND);
				return false;
			}
			while (rs.next()) {
				ipAddress = rs.getString("INET_NTOA(ip_address)");
				String timeStamp = rs.getString("date_time");
				logger.info("IP Address : " + ipAddress+ " | Timestamp : " + timeStamp);
			}
		} catch (NullPointerException | SQLException e) {
			logger.error(e.getMessage());
		}
		
		return true;
	}*/
	
	private void executeInsertBatch(final List<ServerRequest> requests){
		
		 jdbcTemplate.batchUpdate(INSERT_REQUEST_LOG_SQL, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ServerRequest request = requests.get(i);
				ps.setObject(1, new java.sql.Timestamp(request.getDate().getTime()));
				ps.setString(2, request.getIp());
				ps.setString(3, request.getRequestMethod() );
				ps.setString(4, request.getStatus() );
				ps.setString(5, request.getUserAgent() );
			}
					
			@Override
			public int getBatchSize() {
				return requests.size();
			}

		  });
		 
	}

}
