package com.camping.common.steps;

import static io.restassured.RestAssured.given;

import com.camping.common.support.CommonContext;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class Hooks {

    @BeforeAll
    public static void beforeAll() {
        initAdminToken();
    }

    @Before
    public void before() {
        initCommonContext();
    }

    private static void initAdminToken() {
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(CommonContext.ADMIN_BASE_URL)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        CommonContext.adminToken = given().spec(spec)
                .body(Map.of("username", "admin", "password", "admin123"))
                .post("/auth/login").then().extract().cookie("AUTH_TOKEN");
    }

    private static void initCommonContext() {
        CommonContext.lastResponse = null;
        CommonContext.lastParams = new HashMap<>();
    }
}
