package com.camping.tests.helpers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class KioskApiHelper {
    private static final String KIOSK_BASE_URL = System.getProperty("KIOSK_BASE_URL");

    public static Response getProductList() {
        return given()
                .when()
                .get(KIOSK_BASE_URL + "/api/products");
    }
}
