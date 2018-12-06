package com.ef.repository;

import java.util.Date;
import java.util.List;

import com.ef.exception.DBOperationException;
import com.ef.model.BlockReason;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.LogEntry;
import com.ef.observer.Observable;

public interface ParserRepo extends Observable<String> {
	
	public int saveLogEntries(List<LogEntry> entries);
	
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs, Date endDate);

	public int saveBlockedIPs(List<BlockedIP> blockedIPs, BlockReason blockReason) throws DBOperationException;
	
	public long saveBlockReason(BlockReason blockReason);
	
	public BlockReason findBlockReason(CommandLineArgs commandLineArgs) throws DBOperationException;
	
}
