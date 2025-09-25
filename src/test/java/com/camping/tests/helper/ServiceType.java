package com.camping.tests.helper;

public enum ServiceType {
    KIOSK("kiosk", 18081),
    ADMIN("admin", 18082),
    RESERVATION("reservation", 18083);

    private final String serviceName;
    private final int port;

    ServiceType(String serviceName, int port) {
        this.serviceName = serviceName;
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUrl() {
        return "http://localhost:" + port;
    }
}