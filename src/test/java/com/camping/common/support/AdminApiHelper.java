package com.camping.common.support;

import static io.restassured.RestAssured.given;

import io.cucumber.core.options.CurlOption.HttpMethod;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public final class AdminApiHelper {

    public static Response request(HttpMethod httpMethod, String url, Object body) {
        RequestSpecification requestSpec = prepareRequest(CommonContext.adminToken, body);

        if (httpMethod == HttpMethod.GET) {
            return requestSpec.get(url);
        } else if (httpMethod == HttpMethod.POST) {
            return requestSpec.post(url);
        } else if (httpMethod == HttpMethod.PUT) {
            return requestSpec.put(url);
        } else if (httpMethod == HttpMethod.DELETE) {
            return requestSpec.delete(url);
        } else if (httpMethod == HttpMethod.PATCH) {
            return requestSpec.patch(url);
        } else {
            throw new IllegalArgumentException("Http method " + httpMethod + " is not supported.");
        }
    }

    private static RequestSpecification prepareRequest(String authToken, Object body) {
        RequestSpecification authorizedRequestSpec = given()
                .header("Authorization", "Bearer " + authToken);

        if (body != null) {
            authorizedRequestSpec.body(body);
        }

        return authorizedRequestSpec;
    }
}
