package com.ef.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.ef.exception.DBOperationException;
import com.ef.model.BlockReason;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.LogEntry;
import com.ef.observer.Observer;
import com.ef.repository.helper.DbQueryHelper;

@Repository
public class ParserRepoImpl implements ParserRepo {

	private final Logger logger = LoggerFactory.getLogger(ParserRepoImpl.class);
	private final List<Observer<String>> observers = new ArrayList<>();

	@Autowired
	@Qualifier("customJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DbQueryHelper dbQueryHelper;

	@Override
	public int saveLogEntries(final List<LogEntry> entries) {

		int[] affectedRows = jdbcTemplate.batchUpdate(dbQueryHelper.getQuery("INSERT_LOG_ENTRY"),
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						LogEntry entry = entries.get(i);
						ps.setObject(1, new java.sql.Timestamp(entry.getDate().getTime()));
						ps.setString(2, entry.getIP());
						ps.setString(3, entry.getRequestMethod());
						ps.setString(4, entry.getStatus());
						ps.setString(5, entry.getUserAgent());
					}

					@Override
					public int getBatchSize() {
						return entries.size();
					}
				});
		return countAffectedRows(affectedRows);

	}

	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs, Date endDate) {

		List<BlockedIP> blockedIPs = new LinkedList<>();
		
		logger.info("Fetching Blocked IPs from DB...");
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(dbQueryHelper.getQuery("SELECT_BLOCKED_IP"),
				new Object[] { commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold() });

		for (Map<String, Object> row : rows) {
			BlockedIP blockedIP = new BlockedIP();
			blockedIP.setIP(row.get("IP").toString());
			String numRequest = row.get("IP_COUNT").toString();
			blockedIP.setNumberOfRequests(Integer.valueOf(numRequest));
			// String reason = "Exceeded " + commandLineArgs.getDuration() + " " + commandLineArgs.getThreshold() + " threshold";
			//blockedIP.setReason(blockReason);
			blockedIPs.add(blockedIP);
			notifyObservers("IP: " + blockedIP.getIP() + " | NumberOfRequests: " + blockedIP.getNumberOfRequests());
		}

		return blockedIPs;

	}
	
	@Override
	public BlockReason findBlockReason(CommandLineArgs commandLineArgs) throws DBOperationException {
		try {
			return jdbcTemplate.queryForObject(
				dbQueryHelper.getQuery("SELECT_BLOCK_REASON"), new Object[] { 
						new java.sql.Timestamp(commandLineArgs.getStartDate().getTime()),
						commandLineArgs.getDuration().name(),
						commandLineArgs.getThreshold() }, 
				new BeanPropertyRowMapper<BlockReason>(BlockReason.class));
		} catch (DataAccessException e) {
			throw new DBOperationException("No BlockReason found: " + e.getMessage());
		}
	}
	
	@Override
	public long saveBlockReason(BlockReason blockReason) { 

		GeneratedKeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
		    @Override
		    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
		        PreparedStatement statement = con.prepareStatement(dbQueryHelper.getQuery("INSERT_BLOCK_REASON"), Statement.RETURN_GENERATED_KEYS);
		        statement.setTimestamp(1, new java.sql.Timestamp(blockReason.getStartDate().getTime()));
		        statement.setString(2, blockReason.getDuration().name());
		        statement.setInt(3, blockReason.getThreshold());
		        return statement;
		    }
		}, holder);
		System.out.println("saveBlockReason: " + holder.getKey().longValue());
		return holder.getKey().longValue();
	}

	@Override
	public int saveBlockedIPs(List<BlockedIP> blockedIPs, BlockReason blockReason) throws DBOperationException {
		
		try {
			int[] updateResult = jdbcTemplate.batchUpdate(dbQueryHelper.getQuery("INSERT_BLOCKED_IP"),
					new BatchPreparedStatementSetter() {
	
						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {
							BlockedIP blockedIP = blockedIPs.get(i);
							ps.setString(1, blockedIP.getIP());
							ps.setLong(2, blockedIP.getNumberOfRequests());
							ps.setLong(3, blockReason.getId());
						}
	
						@Override
						public int getBatchSize() {
							return blockedIPs.size();
						}
					});
			return countAffectedRows(updateResult);
		} catch (DataAccessException e){
			throw new DBOperationException(e.getMessage());
		}

	}

	private int countAffectedRows(int[] affectedRows) {
		int affectedCount = 0;
		for (int affectedRow : affectedRows) {
			if (affectedRow != 0 && affectedRow != Statement.EXECUTE_FAILED) {
				affectedCount++;
			}
		}
		return affectedCount;
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