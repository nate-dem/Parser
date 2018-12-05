package com.ef.model;

public class BlockedIP {

	private String IP;
	private Integer numberOfRequests;
	// change reason to Integer!!
	private String reason;

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

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
