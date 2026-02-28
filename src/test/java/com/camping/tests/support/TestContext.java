package com.camping.tests.support;

import io.restassured.response.Response;

public class TestContext {

    private static final ThreadLocal<TestContext> CURRENT =
            ThreadLocal.withInitial(TestContext::new);

    public static TestContext current() {
        return CURRENT.get();
    }

    private Response lastResponse;

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }
}
