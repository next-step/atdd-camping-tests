package com.camping.tests.steps;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class AdminStepContext {
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REQUEST_SPECIFICATION_KEY = "requestSpecification";
    private static final String BASE_URL = "http://localhost:18082";
    private static final String BASE_CONTENT_TYPE = "application/json";

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void setAccessToken(String value) {
        context.get().put(ACCESS_TOKEN_KEY, value);
    }

    public static String getAccessToken() {
        return (String) context.get().get(ACCESS_TOKEN_KEY);
    }

    public static void setSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        RequestSpecification requestSpecification = builder.setBaseUri(BASE_URL)
                .setContentType(BASE_CONTENT_TYPE)
                .log(LogDetail.ALL)
                .build();

        context.get().put(REQUEST_SPECIFICATION_KEY, requestSpecification);
    }

    public static RequestSpecification getRequestSpecification() {
        return (RequestSpecification) context.get().get(REQUEST_SPECIFICATION_KEY);
    }

    public static RequestSpecification getRequestSpecificationWithAccessToken() {
        return getRequestSpecification().header("Authorization", "Bearer " + getAccessToken());
    }
}
