package com.ef.util;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Connection;

public class DBConnection {
	private static final String url = "jdbc:mysql://localhost:3306/parser?rewriteBatchedStatements=true";
	private static final String user = "root";
	private static final String password = "root";
	private static final Logger logger = Logger.getLogger(DBConnection.class);
	
	private DBConnection() {}
	
	public static Connection getConnection()  {
		Connection conn = null;
		try {
			conn = (Connection) DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		return conn;
	}
}
