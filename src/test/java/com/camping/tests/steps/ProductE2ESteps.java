package com.camping.tests.steps;

import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만약;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ProductE2ESteps {
    Response response;

    @만약("상품 목록을 조회하기 위해 {string} API를 호출한다")
    public void 상품목록을조회하기위해API를호출한다(String url) {
        System.out.println("# " + url + "에 요청을 보냈다");
        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .when().get(url)
            .then().log().all()
            .extract().response();
        System.out.println("# response: " + response.asString());
    }

    @그러면("성공 응답을 받는다2")
    public void 성공응답을받는다2() {
        response.then().statusCode(200);
    }
}
