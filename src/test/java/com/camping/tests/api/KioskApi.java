package com.camping.tests.api;

import com.camping.tests.context.HttpContext;

import java.util.Map;

import static com.camping.tests.config.TestEnvironment.kioskHost;

@SuppressWarnings("NonAsciiCharacters")
public class KioskApi extends BasicApi {

    public KioskApi(HttpContext httpContext) {
        super(httpContext);
    }

    public void 상품_목록_조회_요청() {
        get(kioskHost(), "/api/products");
    }

    public void 헬스_체크() {
        get(kioskHost(), "/");
    }

    public void 결제_확정_요청(String paymentKey, String orderId, Integer amount) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        post(kioskHost(), "/api/payments/confirm", body);
    }
}
