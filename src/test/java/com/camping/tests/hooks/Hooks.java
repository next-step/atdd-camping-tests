package com.camping.tests.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.context.RequestSpecFactory;
import com.camping.tests.helpers.BaseApiHelper;

public class Hooks {

    @BeforeAll
    public static void cleanDatabase() {
        String dbUrl = System.getProperty("DB_URL", "jdbc:mysql://localhost:3306/atdd");
        String dbUser = System.getProperty("DB_USER", "root");
        String dbPassword = System.getProperty("DB_PASSWORD", "secret");

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             java.sql.Statement stmt = conn.createStatement()) {
            
            stmt.execute("DELETE FROM reservations WHERE id > 10");
            stmt.execute("DELETE FROM sales_records WHERE id > 10");
            stmt.execute("DELETE FROM rental_records WHERE id > 10");
            
        } catch (Exception e) {
            System.err.println("DB cleanup failed: " + e.getMessage());
        }
    }

    @Before
    public void setUp() {
        CommonContextHolder context = CommonContextHolder.getInstance();
        context.setRequestSpec(RequestSpecFactory.create());
        String adminToken = BaseApiHelper.authenticateAndGetToken();
        context.setAdminToken(adminToken);
    }

    @After
    public void tearDown() {
        CommonContextHolder.clear();
    }

}
