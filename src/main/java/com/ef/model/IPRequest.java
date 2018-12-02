package com.ef.model;

public class IPRequest {
	
	private String ip;
	private Integer numberOfRequests;
	
	public IPRequest() {
	}

	public IPRequest(String ip, Integer numberOfRequests) {
		super();
		this.ip = ip;
		this.numberOfRequests = numberOfRequests;
	}

	public String getIp() {
		return ip;
	}

	public Integer getNumberOfRequests() {
		return numberOfRequests;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setNumberOfRequests(Integer numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}
	
}
