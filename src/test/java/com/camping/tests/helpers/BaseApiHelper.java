package com.camping.tests.helpers;

import io.restassured.response.Response;
import io.restassured.http.ContentType;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BaseApiHelper {
    private static final String ADMIN_BASE_URL = System.getProperty("ADMIN_BASE_URL", "http://localhost:8082");

    public static String authenticateAndGetToken() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "admin",
                        "password", "admin123"
                ))
                .when()
                .post(ADMIN_BASE_URL + "/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.getCookie("AUTH_TOKEN");
    }
}
