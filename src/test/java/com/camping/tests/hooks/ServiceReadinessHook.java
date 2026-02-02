package com.camping.tests.hooks;

import com.camping.tests.config.TestConfig;
import io.cucumber.java.BeforeAll;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ServiceReadinessHook {

    @BeforeAll
    public static void waitForServices() {
        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getAdminBaseUrl() + "/login"));

        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getKioskBaseUrl()));

        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getReservationBaseUrl()));

        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getPaymentsBaseUrl() + "/__admin/mappings"));
    }

    private static boolean isServiceReady(String url) {
        try {
            int statusCode = given().get(url).getStatusCode();
            return statusCode >= 200 && statusCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
}