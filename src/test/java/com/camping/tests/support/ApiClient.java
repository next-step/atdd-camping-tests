package com.camping.tests.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private String authToken;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ApiClient withToken(String token) {
        this.authToken = token;
        return this;
    }

    public Response get(String path) {
        return request().get(baseUrl + path);
    }

    public Response post(String path, Object body) {
        return request()
                .body(toJson(body))
                .post(baseUrl + path);
    }

    private RequestSpecification request() {
        RequestSpecification spec = given()
                .contentType("application/json");

        if (authToken != null) {
            spec.header("Authorization", "Bearer " + authToken);
        }

        return spec;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}