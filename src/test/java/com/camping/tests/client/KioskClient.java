package com.camping.tests.client;

import com.camping.tests.CommonContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class KioskClient {
    private static final String kioskBaseUrl = "http://localhost:18081";
    public void getProducts() {
        CommonContext.lastResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .when().get(kioskBaseUrl + "/api/products")
            .then().log().all()
            .extract().response();
    }
}
