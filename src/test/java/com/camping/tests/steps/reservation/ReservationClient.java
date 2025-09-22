package com.camping.tests.steps.reservation;

import com.camping.tests.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.List;

public class ReservationClient {
    private static final RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(Service.RESERVATION.getBaseUrl())
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addFilters(
            List.of(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
            )
        )
        .build();

    public static RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
