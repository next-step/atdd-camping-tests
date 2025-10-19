package com.camping.tests.context;

import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class CommonContextHolder {
    private static final ThreadLocal<CommonContextHolder> context = new ThreadLocal<>();

    private RequestSpecification requestSpec;
    private String adminToken;
    private Response response;

    public static CommonContextHolder getInstance() {
        CommonContextHolder ctx = context.get();
        if (ctx == null) {
            ctx = new CommonContextHolder();
            context.set(ctx);
        }
        return ctx;
    }

    public static void clear() {
        context.remove();
    }

    public RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            throw new IllegalStateException("RequestSpecification is not initialized");
        }
        return requestSpec.contentType(ContentType.JSON);
    }

    public void setRequestSpec(RequestSpecification spec) {
        this.requestSpec = spec;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
