package com.camping.tests.steps.admin;

import com.camping.tests.steps.admin.dto.CreateProductRequest;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

public class AdminProductTestSteps {
    // 상품 등록
    public static Response 상품을_등록한다(CreateProductRequest request) {
        return AdminClient.given()
            .body(request)
            .when().post("/admin/products")
            .thenReturn();
    }

    public static void 상품_등록이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_CREATED);
    }
}
