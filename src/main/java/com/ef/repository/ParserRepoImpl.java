package com.ef.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import com.ef.observer.Observer;
import com.ef.util.DateForamtter;

@Repository
public class ParserRepoImpl implements ParserRepo {

	private final Logger logger = LoggerFactory.getLogger(ParserRepoImpl.class);
	private final List<Observer<String>> observers = new ArrayList<>();
	
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
		
		try (Stream<String> lines = Files.lines(Paths.get(pathToFile))) {

			final AtomicInteger counter = new AtomicInteger();
			List<LogEntry> entries = new ArrayList<>();
			long start = System.currentTimeMillis();
			lines.forEach(line -> {
				String[] lineArr = line.split(env.getProperty("parser.log.file.delimiter"));
				
				if(lineArr.length != 5)
					return;
				
				try {
					
					Date parsedDate = DateForamtter.fromString(lineArr[0], env.getProperty("parser.log.date.format"));

					LogEntry entry = new LogEntry();
					entry.setDate(parsedDate);
					entry.setIP(lineArr[1]);
					entry.setRequestMethod(lineArr[2]);
					entry.setStatus(lineArr[3]);
					entry.setUserAgent(lineArr[4]);
					entries.add(entry);
					
					logger.info(messageSource.getMessage("parser.action.db.save.status", new Object[]{counter.incrementAndGet()}, Locale.US) );
					
					if(entries.size() % 20000 == 0) {
						saveLogsInBatch(entries);
						entries.clear();
					}
						
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			});
			
			if(!entries.isEmpty()) {
				saveLogsInBatch(entries);
				entries.clear();
			}
			if(counter.get() > 0) {
				logger.info(messageSource.getMessage("parser.action.db.save.complete", null, Locale.US));
				logger.info(messageSource.getMessage("parser.action.db.save.elapsed.time", new Object[]{ (System.currentTimeMillis()-start) }, Locale.US));	
			} else {
				logger.info(messageSource.getMessage("parser.action.db.not.saved", null, Locale.US));
			}
			
		} catch (IOException | InvalidPathException  e) {
			throw new InvalidLogFileException("Error saving log: " + e);
		}
		
	}
	
	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs, Date endDate) {
		
		List<BlockedIP> blockedIPs = new LinkedList<>();

		try {
			
			logger.info(messageSource.getMessage("parser.action.find.blocked.ips", null, Locale.US));
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(dbQueryHelper.getQuery("SELECT_BLOCKED_IP"),
					new Object[] { commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold()  } );
			
			for (Map<String, Object> row : rows) {
				// logger.info("Result from SELECT_IP_BY_CRITERIA_SQL {} ", row);
				BlockedIP blockedIP = new BlockedIP();
				blockedIP.setIP(row.get("IP_ADDRESS").toString());
				String numRequest = row.get("COUNT(IP_ADDRESS)").toString();
				blockedIP.setNumberOfRequests(Integer.valueOf(numRequest));
				String reason = messageSource.getMessage("parser.ip.block.reaso", new Object[]{ commandLineArgs.getDuration() }, Locale.US);
				blockedIP.setReason(reason);
				blockedIPs.add(blockedIP);
				notifyObservers("IP: " + blockedIP.getIP() + " | NumberOfRequests: "+ blockedIP.getNumberOfRequests() );
			}
			
			return blockedIPs;
			
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
			throw e;
		}
		
	}
	
	@Override
	public int[] saveBlockedIPs(List<BlockedIP> blockedIPs) {
		
		try {
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
		} catch(DataAccessException e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private void saveLogsInBatch(final List<LogEntry> entries) {
		
		try {
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
		} catch(DataAccessException e) {
			logger.error(e.getMessage());
			throw e;
		}
		 
	}
	
	@Override
	public void notifyObservers(String msg) {
		observers.forEach(o -> o.update(msg));
	}

	@Override
	public void addObserver(Observer<String> observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer<String> observer) {
		observers.remove(observer);
	}

}