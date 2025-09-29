package com.camping.tests.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/atdd";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";

    private static Connection connection;

    public static void initConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void closeConnection() throws SQLException {
        connection.close();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static List<String> getAllTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();

        try (
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SHOW TABLES")
        ) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
        }

        return tableNames;
    }

    public static void truncateAllTables() throws SQLException {
        List<String> tableNames = getAllTableNames();

        try (var statement = connection.createStatement()) {
            // Disable foreign key checks to avoid constraint issues
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Truncate each table
            for (var tableName : tableNames) {
                String truncateQuery = "TRUNCATE TABLE " + tableName;
                statement.execute(truncateQuery);
            }

            // Re-enable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

}
