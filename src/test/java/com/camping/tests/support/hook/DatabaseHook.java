package com.camping.tests.support.hook;

import com.camping.tests.support.helper.DatabaseHelper;
import com.camping.tests.support.helper.ServiceType;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHook {
    private static final Logger log = LoggerFactory.getLogger(DatabaseHook.class);
    private static boolean isInitialized = false;

    @BeforeAll
    public static void setupDatabasesBeforeAllTests() {
        log.info("Setting up shared H2 database before all tests...");
        DatabaseHelper.cleanupAllDatabases();
        DatabaseHelper.initializeDatabase(ServiceType.ADMIN); // Use any service type, it's the same DB

        isInitialized = true;
        log.info("Shared H2 database setup completed");
    }

    @Before(order = 1)
    public void setupDatabaseBeforeEachScenario() {
        if (!isInitialized) {
            log.warn("Database not initialized, running setup...");
            setupDatabasesBeforeAllTests();
        }

        log.info("Cleaning up shared H2 database before scenario...");
        DatabaseHelper.cleanupAllDatabases();
        log.info("Database cleanup completed before scenario");
    }

    public static void verifyDatabaseConnectivity() {
        log.info("Verifying shared H2 database connectivity...");

        boolean isAccessible = DatabaseHelper.isDatabaseAccessible();
        if (isAccessible) {
            log.info("Shared H2 database connectivity verified");
        } else {
            log.error("Shared H2 database connectivity failed");
            throw new RuntimeException("Shared H2 database not accessible");
        }

        log.info("Database connection verified successfully");
    }
}