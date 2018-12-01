package com.ef.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ef.model.ServerRequest;
import com.ef.util.DBConnection;
import com.ef.util.DurationType;
import com.ef.util.ParserConstants;
import com.mysql.jdbc.Connection;
import java.sql.PreparedStatement;

public class ParserRepoImpl implements ParserRepo {

	private final Logger logger = LoggerFactory.getLogger(ParserRepoImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	
	// mysql insert statement.  Avoids insertion of duplicate records based on start_date & ip_address value pairs.
	private final static String INSERT_REQUEST_LOG_SQL = " insert ignore into ServerRequestLogs (start_date, ip_address, request_method, status, user_agent)"
				+ " values (?, INET_ATON(?), ?, ?, ?)";
	
	public ParserRepoImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void saveBlockedIps() {}

	public void saveRequestData(Stream<String> stream) {
		
		//try (Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(query)) {
		try {
			final AtomicInteger counter = new AtomicInteger();
			List<ServerRequest> requests = new ArrayList<>();
			long start = System.currentTimeMillis();
			stream.forEach(str -> {
				String[] trimInput = str.split(ParserConstants.LOG_FILE_DELIMITER);
				
				try {
					Date parsedDate = new SimpleDateFormat(ParserConstants.LOG_DATE_FORMAT).parse(trimInput[0]);
					//preparedStmt.setObject(1, new java.sql.Timestamp(parsedDate.getTime()));
					//preparedStmt.setString(2, trimInput[1]);
					//preparedStmt.setString(3, trimInput[2]);
					//preparedStmt.setString(4, trimInput[3]);
					//preparedStmt.setString(5, trimInput[4]);
					ServerRequest serverRequest = new ServerRequest();
					serverRequest.setDate(parsedDate);
					serverRequest.setIp(trimInput[1]);
					serverRequest.setRequestMethod(trimInput[2]);
					serverRequest.setStatus(trimInput[3]);
					serverRequest.setUserAgent(trimInput[4]);
					requests.add(serverRequest);
					//preparedStmt.addBatch();
					
					logger.info("processed " + counter.incrementAndGet());
					
					//if(counter.get() % 10000 == 0)
						//preparedStmt.executeBatch();
					if(requests.size() % 10000 == 0) {
						executeInsertBatch(requests);
						requests.clear();
					}
						
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			});
			//preparedStmt.executeBatch();
			logger.info(ParserConstants.DB_IMPORT_COMPLETED);
			logger.info("Time Taken = "+(System.currentTimeMillis()-start)+"ms");
		} catch (NullPointerException e1) {
			logger.error(e1.getMessage());
			throw e1;
		}

	}
	
	//insert batch example
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
	}

}
