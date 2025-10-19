package com.camping.tests.steps.admin;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminRentalTestSteps {

    public static Response 모든_렌탈을_조회한다() {
        return AdminClient.given()
            .when().get("/admin/rentals")
            .thenReturn();
    }

    public static Response 렌탈을_생성한다(Long reservationId, Long productId, Integer quantity) {
        return AdminClient.given()
            .body(Map.of(
                "reservationId", reservationId,
                "productId", productId,
                "quantity", quantity
            ))
            .when().post("/admin/rentals")
            .thenReturn();
    }

    public static Response 렌탈을_반납_처리한다(Long rentalRecordId) {
        return AdminClient.given()
            .when().patch("/admin/rentals/" + rentalRecordId + "/return")
            .thenReturn();
    }

    public static void 렌탈이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_CREATED);
    }

    public static void 렌탈_반납이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    public static void 렌탈_상품이_등록되었다(Response response, String expectedProductName) {
        response.then().statusCode(HttpStatus.SC_CREATED);

        Map<String, Object> responseBody = response.as(Map.class);
        String actualProductName = (String) responseBody.get("productName");

        assertThat(actualProductName).isEqualTo(expectedProductName);
    }

    public static void 렌탈_상태가_반납완료로_변경되었다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);

        Map<String, Object> responseBody = response.as(Map.class);
        Boolean isReturned = (Boolean) responseBody.get("isReturned");

        assertThat(isReturned).isTrue();
    }

    public static Long 렌탈_ID를_가져온다(Response response) {
        response.then().statusCode(HttpStatus.SC_CREATED);

        Map<String, Object> responseBody = response.as(Map.class);
        return ((Number) responseBody.get("id")).longValue();
    }

    public static Long 상품_ID를_가져온다(Response response, String productName) {
        response.then().statusCode(HttpStatus.SC_OK);

        Object[] products = response.as(Object[].class);
        for (Object productObj : products) {
            Map<String, Object> product = (Map<String, Object>) productObj;
            if (productName.equals(product.get("name"))) {
                return ((Number) product.get("id")).longValue();
            }
        }
        throw new AssertionError("상품을 찾을 수 없습니다: " + productName);
    }

    public static void 상품_재고가_확인된다(Response response, String productName, Integer expectedStock) {
        response.then().statusCode(HttpStatus.SC_OK);

        Object[] products = response.as(Object[].class);
        for (Object productObj : products) {
            Map<String, Object> product = (Map<String, Object>) productObj;
            if (productName.equals(product.get("name"))) {
                Integer actualStock = ((Number) product.get("stockQuantity")).intValue();
                assertThat(actualStock).isEqualTo(expectedStock);
                return;
            }
        }
        throw new AssertionError("상품을 찾을 수 없습니다: " + productName);
    }
}