package com.camping.tests.steps;

import java.util.Map;

public class TestContext {
    private Map<String, String> authCookies;
    private String authToken;

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public String authToken() {
        return authToken;
    }

    public String adminBaseUrl() {
        return System.getProperty("admin.base.url");
    }

    public String kioskBaseUrl() {
        return System.getProperty("kiosk.base.url");
    }

    public void setAuthCookies(Map<String, String> cookies) {
        this.authCookies = cookies;
    }

    public Map<String, String> authCookies() {
        return authCookies;
    }
}
