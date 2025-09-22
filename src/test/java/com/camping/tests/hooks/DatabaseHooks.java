package com.camping.tests.hooks;

import com.camping.tests.utils.DatabaseHelper;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseHooks {

    @BeforeAll
    public static void setUpDatabase() throws SQLException {
        DatabaseHelper.initConnection();
        log.info("=== Database Connection Established ===");
    }

    @Before
    public void setUp() throws SQLException {
        DatabaseHelper.truncateAllTables();
        log.info("=== Test Setup: Truncated all tables ===");
    }

    @AfterAll
    public static void tearDownDatabase() throws SQLException {
        DatabaseHelper.closeConnection();
        log.info("=== Database Connection Closed ===");
    }
}
