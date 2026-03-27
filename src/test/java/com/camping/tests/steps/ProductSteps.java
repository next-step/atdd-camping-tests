package com.camping.tests.steps;

import com.camping.tests.support.TestConfig;
import com.camping.tests.support.TestContext;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductSteps {

    @만약("키오스크에서 상품 목록을 요청한다")
    public void 키오스크_상품_목록_요청() {
        Response response = RestAssured.given()
                .get(TestConfig.KIOSK_BASE_URL + "/api/products");
        TestContext.current().setLastResponse(response);
    }

    @그리고("상품 목록에 {string} 상품이 포함되어 있다")
    public void 상품_목록에_상품이_포함(String productName) {
        List<Map<String, Object>> products = TestContext.current().getLastResponse()
                .jsonPath().getList("");
        boolean found = products.stream()
                .anyMatch(p -> productName.equals(p.get("name")));
        assertTrue(found, "상품 목록에 '" + productName + "' 상품이 없습니다. 실제: " + products);
    }

    @그리고("상품 목록의 개수는 {int}개이다")
    public void 상품_목록_개수_확인(int expectedCount) {
        List<Map<String, Object>> products = TestContext.current().getLastResponse()
                .jsonPath().getList("");
        assertEquals(expectedCount, products.size(),
                "상품 개수가 다릅니다. 실제: " + products.size());
    }
}
