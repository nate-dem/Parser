package com.ef.repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import com.ef.util.DBConnection;
import com.ef.util.Helper;
import com.ef.util.Property;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class ParserRepoImpl implements ParserRepo{
	private Helper helper;
	private final Logger logger = Logger.getLogger(ParserRepo.class);
	
	public ParserRepoImpl() {
		helper = new Helper();
	}

	public void saveRequestData(Stream<String> stream) {

		// mysql insert statement.  Avoids insertion of duplicate records based on date_time & ip_address value pairs.
		String query = " insert ignore into request_log (date_time, ip_address, request_method, status, user_agent)"
				+ " values (?, INET_ATON(?), ?, ?, ?)";
		
		try (Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(query)) {
			 
			final AtomicInteger counter = new AtomicInteger();
			long start = System.currentTimeMillis();
			stream.forEach(str -> {
				String[] trimInput = str.split("\\|");
				
				try {
					Date parsedDate = helper.getDateFormat().parse(trimInput[0]);
					preparedStmt.setObject(1, new java.sql.Timestamp(parsedDate.getTime()));
					preparedStmt.setString(2, trimInput[1]);
					preparedStmt.setString(3, trimInput[2]);
					preparedStmt.setString(4, trimInput[3]);
					preparedStmt.setString(5, trimInput[4]);

					preparedStmt.addBatch();
					
					logger.info("processed " + counter.incrementAndGet());
					
					if(counter.get() % 10000 == 0)
						preparedStmt.executeBatch();
					
				} catch (ParseException | SQLException e) {
					logger.error(e.getMessage());
				}
			});
			preparedStmt.executeBatch();
			logger.info(Property.DB_IMPORT_COMPLETED);
			logger.info("Time Taken = "+(System.currentTimeMillis()-start)+"ms");
		} catch (NullPointerException | SQLException e1) {
			logger.error(e1.getMessage());
		}

	}

	public boolean filterRequestData(Date parsedStartDate, Date parsedEndDate, String duration, int threshold) {

		String selectSQL = "SELECT id, INET_NTOA(ip_address), date_time, COUNT(ip_address) FROM request_log WHERE date_time >= ? AND date_time <= ? GROUP BY ip_address HAVING COUNT(ip_address) >= ?  ";
		
		try(Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(selectSQL)) {
			
			preparedStmt.setObject(1, parsedStartDate);
			preparedStmt.setObject(2, parsedEndDate);
			preparedStmt.setInt(3, threshold);

			logger.info(Property.PROCESSING_QUERY);
			// execute select SQL statement
			ResultSet rs = preparedStmt.executeQuery();

			if (!rs.isBeforeFirst()) {
				logger.info(Property.NO_RESULT_FOUND);
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
				preparedInsertStmt.setString(3, duration.toUpperCase());
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
	
	public boolean filterAccessLogFile(String pathToFile, Date parsedStartDate, Date parsedEndDate, int threshold) {
		
		if (!new File(pathToFile).exists()){
			logger.error(Property.INVALID_FILE);
			return false;
		}
		
		logger.info(Property.PROCESSING_QUERY);
		
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			Map<String, Long> counting = 
				stream.filter(t -> {
					String[] trimInput = t.split("\\|");
					try {
						Date parsedDate = helper.getDateFormat().parse(trimInput[0]);
						if (parsedDate.compareTo(parsedStartDate) >= 0 && parsedDate.compareTo(parsedEndDate) <= 0) {
							return true;
						}
					} catch (ParseException e) {
						logger.error(e.getMessage());
					}
					return false;
				})
			  .map(str -> str.split("\\|"))
			  .map(str -> str[1])
			  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	
			boolean hasResult = false;
			
			for(Entry<String, Long> k : counting.entrySet()) {
				if (k.getValue() >= threshold) {
					logger.info(k.getKey());
					hasResult = true;
				}
			}
			
			if (hasResult) {
				return true;
			}
			else {
				logger.info(Property.NO_RESULT_FOUND);
				return false;
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return false;
	}

	public boolean filterByIP(String ipAddress) {

		String selectSQL = "SELECT id, INET_NTOA(ip_address), date_time FROM request_log WHERE ip_address = INET_ATON(?) ";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(selectSQL)) {
			preparedStmt.setObject(1, ipAddress);
			// execute select SQL statement
			ResultSet rs = preparedStmt.executeQuery();
			logger.info(Property.PROCESSING_QUERY);
			
			if (!rs.isBeforeFirst()) {
				logger.info(Property.NO_RESULT_FOUND);
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
