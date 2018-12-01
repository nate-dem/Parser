package com.ef.model;

import java.util.Date;

import com.ef.util.DurationType;

public class CommandLineArgs {
	private Date startDate;
	private DurationType duration;
	private Integer threshold;
	private String accesslog;
	
	public CommandLineArgs(Date startDate, DurationType duration, Integer threshold, String accesslog) {
		super();
		this.startDate = startDate;
		this.duration = duration;
		this.threshold = threshold;
		this.accesslog = accesslog;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	public DurationType getDuration() {
		return duration;
	}
	public Integer getThreshold() {
		return threshold;
	}
	public String getAccesslog() {
		return accesslog;
	}
	
}
