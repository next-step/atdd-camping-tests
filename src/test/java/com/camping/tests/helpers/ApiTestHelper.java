package com.camping.tests.helpers;

import io.restassured.RestAssured;
import org.awaitility.Awaitility;

import java.time.Duration;

public class ApiTestHelper {

    public static String resolveBaseUrl(String envKey, String defaultUrl) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env;
        }

        String prop = System.getProperty(envKey);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return defaultUrl;
    }

    public static String buildUrl(String baseUrl, String endpoint) {
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return baseUrl + endpoint;
    }

    public static void assertGetSuccess(String url) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestAssured.given()
                            .when()
                            .get(url)
                            .then()
                            .statusCode(200);
                });
    }
}
