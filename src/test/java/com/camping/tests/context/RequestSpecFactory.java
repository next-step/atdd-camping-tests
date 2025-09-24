package com.camping.tests.context;

import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

public class RequestSpecFactory {

    public static RequestSpecification create() {
        return given();
    }
}