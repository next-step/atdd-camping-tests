package com.camping.tests.helper;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceContext {
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REQUEST_SPECIFICATION_KEY = "requestSpecification";
    private static final String BASE_CONTENT_TYPE = "application/json";

    private static final ThreadLocal<Map<ServiceType, Map<String, Object>>> contexts =
            ThreadLocal.withInitial(() -> new ConcurrentHashMap<>());

    private static Map<String, Object> getServiceContext(ServiceType serviceType) {
        return contexts.get().computeIfAbsent(serviceType, k -> new HashMap<>());
    }

    public static void setAccessToken(ServiceType serviceType, String value) {
        getServiceContext(serviceType).put(ACCESS_TOKEN_KEY, value);
    }

    public static String getAccessToken(ServiceType serviceType) {
        return (String) getServiceContext(serviceType).get(ACCESS_TOKEN_KEY);
    }

    public static void initializeRequestSpec(ServiceType serviceType) {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        RequestSpecification requestSpecification = builder
                .setBaseUri(serviceType.getBaseUrl())
                .setContentType(BASE_CONTENT_TYPE)
                .log(LogDetail.ALL)
                .build();

        getServiceContext(serviceType).put(REQUEST_SPECIFICATION_KEY, requestSpecification);
    }

    public static RequestSpecification getRequestSpecification(ServiceType serviceType) {
        RequestSpecification spec = (RequestSpecification) getServiceContext(serviceType).get(REQUEST_SPECIFICATION_KEY);
        if (spec == null) {
            initializeRequestSpec(serviceType);
            spec = (RequestSpecification) getServiceContext(serviceType).get(REQUEST_SPECIFICATION_KEY);
        }
        return spec;
    }

    public static RequestSpecification getRequestSpecificationWithAccessToken(ServiceType serviceType) {
        String accessToken = getAccessToken(serviceType);
        if (accessToken == null) {
            accessToken = "dummy-token";
        }
        return getRequestSpecification(serviceType).header("Authorization", "Bearer " + accessToken);
    }

    public static void clearContext() {
        contexts.get().clear();
    }

    public static void clearServiceContext(ServiceType serviceType) {
        contexts.get().remove(serviceType);
    }
}