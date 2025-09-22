package com.camping.tests.steps.admin;

import com.camping.tests.steps.admin.dto.AdminProductDetail;
import com.camping.tests.steps.admin.dto.CreateAdminProductRequest;
import com.camping.tests.steps.admin.dto.UpdateAdminProductRequest;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

public class AdminProductTestSteps {
    // 상품 등록
    public static Response 상품을_등록한다(CreateAdminProductRequest request) {
        return AdminClient.given()
            .body(request)
            .when().post("/admin/products")
            .thenReturn();
    }

    public static void 상품_등록이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_CREATED);
    }

    // 상품 수정
    public static Response 상품_이름을_수정한다(long productId, UpdateAdminProductRequest request) {
        return AdminClient.given()
            .body(request)
            .when().put("/admin/products/" + productId)
            .thenReturn();
    }

    public static void 상품_정보_수정이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    // 데이터 변환
    public static AdminProductDetail 상품_정보를_가져온다(Response response) {
        return response.as(AdminProductDetail.class);
    }
}
