package com.camping.common.support;

import static io.restassured.RestAssured.given;

import io.cucumber.core.options.CurlOption.HttpMethod;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public final class ApiHelper {

    public static Response request(HttpMethod httpMethod, String url, Object body) {
        RequestSpecification requestSpec = prepareRequest(body);
        return perform(httpMethod, url, requestSpec).then().log().all()
                .extract().response();
    }

    private static RequestSpecification prepareRequest(Object body) {
        RequestSpecification preparedRequestSpec = given(RequestSpecFactory.create());

        if (body != null) {
            preparedRequestSpec.body(body);
        }

        return preparedRequestSpec;
    }

    public static Response adminRequest(HttpMethod httpMethod, String url, Object body) {
        RequestSpecification requestSpec = prepareAdminRequest(CommonContext.adminToken, body);
        return perform(httpMethod, url, requestSpec).then().log().all()
                .extract().response();
    }

    private static RequestSpecification prepareAdminRequest(String authToken, Object body) {
        RequestSpecification authorizedRequestSpec = given(RequestSpecFactory.create())
                .header("Authorization", "Bearer " + authToken);

        if (body != null) {
            authorizedRequestSpec.body(body);
        }

        return authorizedRequestSpec;
    }

    private static Response perform(HttpMethod httpMethod, String url, RequestSpecification requestSpec) {
        if (httpMethod == HttpMethod.GET) {
            return requestSpec.log().all().get(url);
        } else if (httpMethod == HttpMethod.POST) {
            return requestSpec.log().all().post(url);
        } else if (httpMethod == HttpMethod.PUT) {
            return requestSpec.log().all().put(url);
        } else if (httpMethod == HttpMethod.DELETE) {
            return requestSpec.log().all().delete(url);
        } else if (httpMethod == HttpMethod.PATCH) {
            return requestSpec.log().all().patch(url);
        } else {
            throw new IllegalArgumentException("Http method " + httpMethod + " is not supported.");
        }
    }
}
