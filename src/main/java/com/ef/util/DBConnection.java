package com.ef.util;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Connection;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/parser?rewriteBatchedStatements=true";
	private static final String USER = "root";
	private static final String PASSWORD = "root";
	private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);
	
	private DBConnection() {}
	
	public static Connection getConnection()  {
		Connection conn = null;
		try {
			conn = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}
		return conn;
	}
}
