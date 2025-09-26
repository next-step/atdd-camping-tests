package com.camping.tests.steps;

import com.camping.tests.CommonContext;
import com.camping.tests.client.KioskClient;
import io.cucumber.java.ko.만약;

public class ProductE2ESteps {
    private KioskClient kioskClient = new KioskClient();

    @만약("상품 목록을 조회하기 위해 상품목록조회 API를 호출한다")
    public void 상품목록을_조회하기_위해_상품목록조회_API를_호출한다() {
        kioskClient.getProducts();
        System.out.println("# response: " + CommonContext.lastResponse.asString());
    }
}
