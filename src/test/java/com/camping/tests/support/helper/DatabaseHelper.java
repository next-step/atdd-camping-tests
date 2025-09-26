package com.camping.tests.support.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper {
    private static final Logger log = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final String H2_URL = "jdbc:h2:mem:admindb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    public static void cleanupAllDatabases() {
        log.info("Cleaning up shared H2 database (admindb)");
        cleanupDatabase();
    }

    private static void cleanupDatabase() {
        log.info("Cleaning up H2 database: admindb");

        try (Connection connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Disable foreign key constraints
            statement.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // Drop all tables
            dropAllTables(statement);

            // Reset sequences for auto-increment columns
            resetSequences(statement);

            // Re-enable foreign key constraints
            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");

            log.info("Database cleanup completed for admindb");

        } catch (SQLException e) {
            log.warn("Failed to cleanup database admindb - {}", e.getMessage());
        }
    }

    private static void dropAllTables(Statement statement) throws SQLException {
        // Get all table names from information_schema
        var resultSet = statement.executeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
        );

        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            try {
                statement.execute("DROP TABLE IF EXISTS " + tableName + " CASCADE");
                log.debug("Dropped table: {}", tableName);
            } catch (SQLException e) {
                log.warn("Failed to drop table: {} - {}", tableName, e.getMessage());
            }
        }

        resultSet.close();
    }

    private static void resetSequences(Statement statement) throws SQLException {
        log.info("Resetting sequences...");

        // Get all sequences from information_schema
        var sequenceResultSet = statement.executeQuery(
                "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = 'PUBLIC'"
        );

        int sequenceCount = 0;
        while (sequenceResultSet.next()) {
            String sequenceName = sequenceResultSet.getString("SEQUENCE_NAME");
            sequenceCount++;
            try {
                statement.execute("ALTER SEQUENCE " + sequenceName + " RESTART WITH 1");
                log.info("Reset sequence: {}", sequenceName);
            } catch (SQLException e) {
                log.warn("Failed to reset sequence: {} - {}", sequenceName, e.getMessage());
            }
        }

        if (sequenceCount == 0) {
            log.info("No sequences found to reset");
        } else {
            log.info("Reset {} sequences", sequenceCount);
        }

        sequenceResultSet.close();
    }

    public static void initializeDatabase(ServiceType serviceType) {
        log.info("Initializing H2 database: admindb");

        try (Connection connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Database will be initialized by the application on startup
            // This method is here for future extensibility if manual schema creation is needed

            log.info("Database initialization completed for admindb");

        } catch (SQLException e) {
            log.error("Failed to initialize database admindb - {}", e.getMessage());
        }
    }

    public static boolean isDatabaseAccessible() {
        try (Connection connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD)) {
            return connection.isValid(5);
        } catch (SQLException e) {
            log.warn("Database admindb not accessible - {}", e.getMessage());
            return false;
        }
    }
}