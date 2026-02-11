package com.camping.tests.steps;

import com.camping.tests.api.ProductApi;
import com.camping.tests.steps.context.TestContext;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductListSteps {

    private static final String KIOSK_BASE_URL =
            System.getenv("KIOSK_BASE_URL") != null
                    ? System.getenv("KIOSK_BASE_URL")
                    : "http://localhost:18081";

    @Autowired
    private TestContext testContext;

    @Autowired
    private ProductApi productApi;

    @만약("kiosk의 상품 목록 API를 호출한다")
    public void kiosk의_상품_목록_API를_호출한다() {
        testContext.setResponse(productApi.상품_목록_조회(KIOSK_BASE_URL));
    }

    @그러면("상태코드 {int}을 받는다")
    public void 상태코드를_받는다(int expectedStatus) {
        assertEquals(expectedStatus, testContext.getResponse().statusCode());
    }

    @그리고("응답에 {int}개 이상의 상품이 포함되어 있다")
    public void 응답에_N개_이상의_상품이_포함되어_있다(int minCount) {
        int size = testContext.getResponse().jsonPath().getList("$").size();
        assertTrue(size >= minCount, "상품 수가 " + minCount + "개 이상이어야 하지만 " + size + "개입니다");
    }

    @그리고("각 상품에 id, name, price 필드가 존재한다")
    public void 각_상품에_필드가_존재한다() {
        var products = testContext.getResponse().jsonPath().getList("$");
        for (int i = 0; i < products.size(); i++) {
            assertNotNull(testContext.getResponse().jsonPath().get("[" + i + "].id"), i + "번째 상품에 id가 없습니다");
            assertNotNull(testContext.getResponse().jsonPath().get("[" + i + "].name"), i + "번째 상품에 name이 없습니다");
            assertNotNull(testContext.getResponse().jsonPath().get("[" + i + "].price"), i + "번째 상품에 price가 없습니다");
        }
    }
}
