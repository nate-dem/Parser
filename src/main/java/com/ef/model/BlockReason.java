package com.ef.model;

import java.util.Date;

public class BlockReason {
	
	private long id;
	private DurationType duration;
	private int threshold;
	private Date startDate;

	public BlockReason(){
	}
	
	public BlockReason(Date startDate, DurationType duration, int threshold) {
		super();
		this.startDate = startDate;
		this.duration = duration;
		this.threshold = threshold;
	}
	
	public long getId(){
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setDuration(DurationType duration) {
		this.duration = duration;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public DurationType getDuration() {
		return duration;
	}

	public int getThreshold() {
		return threshold;
	}

	public Date getStartDate() {
		return startDate;
	}

	@Override
	public String toString() {
		return "BlockReason [id=" + id + ", duration=" + duration + ", threshold=" + threshold + ", startDate="
				+ startDate + "]";
	}
	
}
