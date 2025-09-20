package com.camping.tests.steps;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Service {
    KIOSK("http://localhost:8080"),
    ADMIN("http://localhost:8081"),
    RESERVATION("http://localhost:8082");

    private final String defaultBaseUrl;

    public String getBaseUrl() {
        var env = "%s_BASE_URL".formatted(this.name());
        return System.getenv().getOrDefault(env, defaultBaseUrl);
    }
}
