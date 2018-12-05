package com.ef.model;

import java.util.Date;

public class LogEntry {

	private Date date;
	private String IP;
	private String requestMethod;
	private String status;
	private String userAgent;

	public LogEntry() {
	}

	public LogEntry(Date date, String IP, String requestMethod, String status, String userAgent) {
		super();
		this.date = date;
		this.IP = IP;
		this.requestMethod = requestMethod;
		this.status = status;
		this.userAgent = userAgent;
	}

	public Date getDate() {
		return date;
	}

	public String getIP() {
		return IP;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public String getStatus() {
		return status;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setIP(String IP) {
		this.IP = IP;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String toString() {
		return "ServerRequest [date=" + date + ", IP=" + IP + ", requestDetail=" + requestMethod + ", userAgent="
				+ userAgent + "]";
	}

}
