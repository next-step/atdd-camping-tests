package com.camping.tests.context;

import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class CommonContext {
    private static final ThreadLocal<RequestSpecification> requestSpec = new ThreadLocal<>();
    private static String adminToken;
    private Response response;

    public static RequestSpecification getRequestSpec() {
        RequestSpecification spec = requestSpec.get();
        return spec.contentType(ContentType.JSON);
    }

    public static void setRequestSpec(RequestSpecification spec) {
        requestSpec.set(spec);
    }

    public static String getAdminToken() {
        return adminToken;
    }

    public static void setAdminToken(String adminToken) {
        CommonContext.adminToken = adminToken;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
