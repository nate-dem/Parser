package com.ef.model;

import java.util.Date;

public class ServerRequest {
	private Date date;
	private String ip;
	private String requestMethod;
	private String status;
	private String userAgent;
	
	public ServerRequest() {
	}

	public ServerRequest(Date date, String ip, String requestMethod, String status, String userAgent) {
		super();
		this.date = date;
		this.ip = ip;
		this.requestMethod = requestMethod;
		this.status = status;
		this.userAgent = userAgent;
	}

	public Date getDate() {
		return date;
	}
	public String getIp() {
		return ip;
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

	public void setIp(String ip) {
		this.ip = ip;
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
		return "ServerRequest [date=" + date + ", ip=" + ip + ", requestDetail=" + requestMethod 
				+ ", userAgent=" + userAgent + "]";
	}
	
}
