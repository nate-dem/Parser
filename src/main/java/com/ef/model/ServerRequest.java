package com.ef.model;

import java.util.Date;

public class ServerRequest {
	private Date date;
	private String ip;
	private String request;
	private String status;
	private String userAgent;
	
	public ServerRequest(Date date, String ip, String request, String status, String userAgent) {
		super();
		this.date = date;
		this.ip = ip;
		this.request = request;
		this.status = status;
		this.userAgent = userAgent;
	}

	public Date getDate() {
		return date;
	}
	public String getIp() {
		return ip;
	}
	public String getRequest() {
		return request;
	}
	public String getStatus() {
		return status;
	}
	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public String toString() {
		return "ServerRequest [date=" + date + ", ip=" + ip + ", request=" + request + ", userAgent="
				+ userAgent + "]";
	}
	
}
