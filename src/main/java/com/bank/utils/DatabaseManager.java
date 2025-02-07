package com.bank.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/banking";
    private static final String JDBC_USERNAME = "";
    private static final String JDBC_PASSWORD = "";

    private DatabaseManager() {
        throw new IllegalStateException("Utility class");
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
    }
}
