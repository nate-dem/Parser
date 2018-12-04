package com.ef.repository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.LogEntry;
import com.ef.observer.Observable;
import com.ef.observer.Observer;
import com.ef.util.DateForamtter;
import com.ef.util.ParserConstants;

@Repository
public class ParserRepoImpl implements ParserRepo, Observable {

	private final Logger logger = LoggerFactory.getLogger(ParserRepoImpl.class);
	private final List<Observer> observers = new ArrayList<>();
	
	@Autowired
	@Qualifier("customJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DbQueryHelper dbQueryHelper;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private Environment env;
	
	@Override
	public void saveLog(String pathToFile) throws InvalidLogFileException {
		
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			final AtomicInteger counter = new AtomicInteger();
			List<LogEntry> entries = new ArrayList<>();
			long start = System.currentTimeMillis();
			stream.forEach(str -> {
				String[] trimInput = str.split(ParserConstants.LOG_FILE_DELIMITER);
				
				try {
					
					Date parsedDate = DateForamtter.fromString(trimInput[0], env.getProperty("parser.log.date.format"));
					//Date parsedDate = new SimpleDateFormat(env.getProperty("parser.log.date.format")).parse(trimInput[0]);

					LogEntry serverRequest = new LogEntry();
					serverRequest.setDate(parsedDate);
					serverRequest.setIP(trimInput[1]);
					serverRequest.setRequestMethod(trimInput[2]);
					serverRequest.setStatus(trimInput[3]);
					serverRequest.setUserAgent(trimInput[4]);
					entries.add(serverRequest);
					
					logger.info("processed " + counter.incrementAndGet());
					
					if(entries.size() % 20000 == 0) {
						executeSaveLogBatch(entries);
						entries.clear();
					}
						
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			});
			executeSaveLogBatch(entries);
			entries.clear();
			logger.info(ParserConstants.DB_IMPORT_COMPLETED);
			
			logger.info("Time Taken to saveLog:  {} ms" , (System.currentTimeMillis()-start));

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new InvalidLogFileException(e.getMessage());
		}
		
	}
	
	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs, Date endDate) {
		
		List<BlockedIP> blockedIPs = new LinkedList<>();

		try {
			
			logger.info(ParserConstants.PROCESSING_QUERY);
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(dbQueryHelper.getQuery("SELECT_BLOCKED_IP"),
					new Object[] { commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold()  } );
			
			for (Map<String, Object> row : rows) {
				// logger.info("Result from SELECT_IP_BY_CRITERIA_SQL {} ", row);
				BlockedIP blockedIP = new BlockedIP();
				blockedIP.setIP(row.get("IP_ADDRESS").toString());
				String numRequest = row.get("COUNT(IP_ADDRESS)").toString();
				blockedIP.setNumberOfRequests(Integer.valueOf(numRequest));
				blockedIP.setReason("Request exceeds " + commandLineArgs.getDuration() + " limit");
				blockedIPs.add(blockedIP);
				notifyObservers("IP: " + blockedIP.getIP() + " | NumberOfRequests: "+ blockedIP.getNumberOfRequests() );
			}
			
			return blockedIPs;
			
		} catch (NullPointerException | DataAccessException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
	}
	
	@Override
	public int[] saveBlockedIPs(List<BlockedIP> blockedIPs) {
		
		return jdbcTemplate.batchUpdate(dbQueryHelper.getQuery("INSERT_BLOCKED_IP"), new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BlockedIP blockedIP = blockedIPs.get(i);
				//ps.setObject(1, new java.sql.Timestamp(request.getDate().getTime()));
				ps.setString(1, blockedIP.getIP());
				ps.setLong(2, blockedIP.getNumberOfRequests());
				ps.setString(3, blockedIP.getReason() );
			}

			@Override
			public int getBatchSize() {
				return blockedIPs.size();
			}
		  });
		
	}

	@Override
	public boolean findByIP(String ipAddress) {
		return false;
	}

	/*
	
	public boolean findByIP(String ipAddress) {
		String selectSQL = "SELECT id, INET_NTOA(IP_ADDRESS), date_time FROM request_log WHERE IP_ADDRESS = INET_ATON(?) ";
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
				ipAddress = rs.getString("INET_NTOA(IP_ADDRESS)");
				String timeStamp = rs.getString("date_time");
				logger.info("IP Address : " + ipAddress+ " | Timestamp : " + timeStamp);
			}
		} catch (NullPointerException | SQLException e) {
			logger.error(e.getMessage());
		}
		
		return true;
	}*/
	
	private void executeSaveLogBatch(final List<LogEntry> entries){
		
		 jdbcTemplate.batchUpdate(dbQueryHelper.getQuery("INSERT_LOG_ENTRY"), new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				LogEntry entry = entries.get(i);
				ps.setObject(1, new java.sql.Timestamp(entry.getDate().getTime()));
				ps.setString(2, entry.getIP());
				ps.setString(3, entry.getRequestMethod() );
				ps.setString(4, entry.getStatus() );
				ps.setString(5, entry.getUserAgent() );
			}
					
			@Override
			public int getBatchSize() {
				return entries.size();
			}

		  });
		 
	}
	
	@Override
	public void notifyObservers(String msg) {
		observers.forEach(o -> o.update(msg));
	}

	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

}