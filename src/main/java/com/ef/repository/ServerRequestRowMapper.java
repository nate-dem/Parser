package com.ef.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ef.model.ServerRequest;

public class ServerRequestRowMapper implements RowMapper<ServerRequest> {

	public ServerRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
		ServerRequest request = new ServerRequest();
		request.setIp(String.valueOf(rs.getInt("ip_address")));
		return request;
	}
	
}
