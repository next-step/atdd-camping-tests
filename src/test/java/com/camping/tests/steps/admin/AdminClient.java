package com.camping.tests.steps.admin;

import com.camping.tests.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.List;

public class AdminClient {
    private static RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(Service.ADMIN.getBaseUrl())
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addFilters(
            List.of(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
            )
        )
        .build();

    public static void setAuthToken(String authToken) {
        spec = spec.auth().oauth2(authToken);
    }

    public static RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
