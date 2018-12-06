package com.ef.service;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ef.TestConfig;
import com.ef.model.BlockReason;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.model.LogEntry;
import com.ef.repository.ParserRepo;
import com.ef.util.DateForamtter;
import com.ef.util.ParserConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParserRepoTest {

	static String startDateInp = null;
	static String durationInp = null;
	static String thresholdInp = null;
	static String accesslogInp = null;
	static CommandLineArgs commandLineArgs = null;
	static Date endDate;
	
	static List<LogEntry> entries;
	
	static List<BlockedIP> blockedIPs;
	
	@Autowired
	ParserRepo parserRepo; 
	
	@BeforeClass
	public static void setup() throws ParseException {
		LogEntry entry1 = new LogEntry();
		entry1.setDate(DateForamtter.fromString("2017-01-01 00:00:11.763", ParserConstants.LOG_DATE_FORMAT));
		entry1.setIP("192.168.234.82");
		entry1.setRequestMethod("GET / HTTP/1.1");
		entry1.setStatus("200");
		entry1.setUserAgent("swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0");
		
		LogEntry entry2 = new LogEntry();
		entry2.setDate(DateForamtter.fromString("2017-01-01 00:00:21.164", ParserConstants.LOG_DATE_FORMAT));
		entry2.setIP("192.168.234.82");
		entry2.setRequestMethod("GET / HTTP/1.1");
		entry2.setStatus("200");
		entry2.setUserAgent("swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0");
		
		LogEntry entry3 = new LogEntry();
		entry3.setDate(DateForamtter.fromString("2017-01-01 00:00:23.003", ParserConstants.LOG_DATE_FORMAT));
		entry3.setIP("192.168.169.194");
		entry3.setRequestMethod("GET / HTTP/1.1");
		entry3.setStatus("200");
		entry3.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)..");		
		
		entries = new ArrayList<>(Arrays.asList(entry1, entry2, entry3));
		
		thresholdInp = "2";
		durationInp = "hourly";
		startDateInp = "2017-01-01.00:00:00";
		
		int threshold = Integer.parseInt(thresholdInp);
		Date startDate = DateForamtter.fromString(startDateInp, ParserConstants.START_DATE_FORMAT);
		endDate = DateForamtter.fromString("2017-01-01.01:00:00", ParserConstants.START_DATE_FORMAT);
		commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold, accesslogInp);
		
	}
	
	@Test
	public void step1_saveLogEntries() {
		assertEquals(3, parserRepo.saveLogEntries(entries));
	}

	@Test
	public void step2_findBlockedIPsTest() {
		
		blockedIPs = parserRepo.findBlockedIPs(commandLineArgs, endDate);
				
		List<String> ips =	blockedIPs.stream()
									  .map(BlockedIP::getIP)
									  .collect(Collectors.toList());
		
		assertThat(ips, hasItems("192.168.234.82"));
		
	}

	@Test
	public void step3_saveBlockedIPs() {
		BlockReason blockReason = new BlockReason(commandLineArgs.getStartDate(), commandLineArgs.getDuration(), commandLineArgs.getThreshold() );
		long blockReasonId = parserRepo.saveBlockReason(blockReason);
		int result = parserRepo.saveBlockedIPs(blockedIPs, blockReasonId);
		assertEquals(1, result);
	}
}
