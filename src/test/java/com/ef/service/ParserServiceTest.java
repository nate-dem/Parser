package com.ef.service;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ef.TestConfig;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.util.ParserConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class ParserServiceTest {

	String startDateInp = null;
	String durationInp = null;
	String thresholdInp = null;
	String accesslogInp = null;
	CommandLineArgs commandLineArgs = null;
	
	@Autowired
	ParserService parserService;
	
	@Before
	public void prepare() throws ParseException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("access.log").getFile());
		
		accesslogInp = file.getAbsolutePath();
		thresholdInp = "500";
		durationInp = "daily";
		startDateInp = "2017-01-01.00:00:00";
		
		int threshold = Integer.parseInt(thresholdInp);
		Date startDate= new SimpleDateFormat(ParserConstants.START_DATE_FORMAT).parse(startDateInp);
		
		commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold, accesslogInp);
	}
	
	@Test
	public void findBlockedIPsTest() {
		System.out.println(commandLineArgs);
		
		List<String> ips = parserService.findBlockedIPs(commandLineArgs)
										.stream()
										.map(BlockedIP::getIP)
										.collect(Collectors.toList());
		
		assertThat(ips, hasItems("192.168.102.136"));
		
	}
}
