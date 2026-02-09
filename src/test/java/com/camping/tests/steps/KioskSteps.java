package com.camping.tests.steps;

import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만약;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class KioskSteps {
    private static final Map<String, String> SERVICE_URLS = Map.of(
            "kiosk", System.getProperty("kiosk.base.url",
                    System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18080")),
            "admin", System.getProperty("admin.base.url",
                    System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18081")),
            "reservation", System.getProperty("reservation.base.url",
                    System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18082"))
    );

    private final TestContext context;
    private Response response;

    public KioskSteps(TestContext context) {
        this.context = context;
    }

    @만약("상품 목록을 조회한다")
    public void getProducts() {
        response =
                given()
                        .header("Authorization", "Bearer " + context.authToken())
                        .when()
                        .get(SERVICE_URLS.get("kiosk") + "/api/products");
    }

    @그러면("상품 목록이 정상적으로 조회된다")
    public void verifyProducts() {
        response.then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].price", notNullValue())
                .body("[0].stockQuantity", notNullValue())
                .body("[0].productType", notNullValue());
    }
}
