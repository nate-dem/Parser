package com.ef.service;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ef.TestConfig;
import com.ef.exception.InvalidLogFileException;
import com.ef.exception.ParserServiceException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.repository.ParserRepo;
import com.ef.util.DateForamtter;
import com.ef.util.ParserConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParserServiceTest {

	String startDateInp = null;
	String durationInp = null;
	String thresholdInp = null;
	String accesslogInp = null;
	CommandLineArgs commandLineArgs = null;
	
	@Mock
	ParserRepo parserRepo; 
	
	@InjectMocks
	ParserService parserService = new ParserServiceImpl();
	
	@Before
	public void prepare() throws ParseException {
		MockitoAnnotations.initMocks(this);
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("access-test.log").getFile());
		
		accesslogInp = file.getAbsolutePath();
		thresholdInp = "500";
		durationInp = "daily";
		startDateInp = "2017-01-01.00:00:00";
		
		int threshold = Integer.parseInt(thresholdInp);
		Date startDate = DateForamtter.fromString(startDateInp, ParserConstants.START_DATE_FORMAT);
		commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold, accesslogInp);
		
		Mockito.when(parserRepo.saveLogEntries(Mockito.any())).thenReturn(29445);

	}
	
	@Test
	public void step1_saveLogEntries() throws InvalidLogFileException {
		parserService.saveLogEntries(commandLineArgs.getAccesslog());
	}
	
	@Test
	public void step2_findBlockedIPsTest() throws ParserServiceException {
		
		System.out.println(commandLineArgs);
		
		List<String> ips = parserService.findBlockedIPs(commandLineArgs)
										.stream()
										.map(BlockedIP::getIP)
										.collect(Collectors.toList());
		System.out.println(ips);
		assertThat(ips, hasItems("192.168.102.136"));
		
	}
	
	@Test
	public void step3_saveBlockedIPs() throws InvalidLogFileException {
		
		//parserService.saveBlockedIPs(commandLineArgs.getAccesslog();
	}
}
