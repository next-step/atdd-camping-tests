package com.camping.tests.steps;

import com.camping.tests.CommonContext;
import com.camping.tests.client.KioskClient;
import io.cucumber.java.ko.만약;

public class ProductE2ESteps {
    private KioskClient kioskClient = new KioskClient();

    @만약("상품 목록을 조회하기 위해 {string} API를 호출한다")
    public void 상품목록을_조회하기_위해_API를_호출한다(String urlPath) {
        System.out.println("# " + urlPath + "에 요청을 보냈다");
        kioskClient.getProducts();
        System.out.println("# response: " + CommonContext.lastResponse.asString());
    }
}
