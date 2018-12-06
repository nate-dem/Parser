package com.ef.model;

public class BlockedIP {

	private String IP;
	private Integer numberOfRequests;
	private BlockReason reason;

	public String getIP() {
		return IP;
	}

	public void setIP(String IP) {
		this.IP = IP;
	}

	public Integer getNumberOfRequests() {
		return numberOfRequests;
	}

	public void setNumberOfRequests(Integer numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}

	public BlockReason getReason() {
		return reason;
	}

	public void setReason(BlockReason reason) {
		this.reason = reason;
	}

}
