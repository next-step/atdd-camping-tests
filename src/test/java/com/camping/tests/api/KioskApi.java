package com.camping.tests.api;

import com.camping.tests.context.HttpContext;
import com.camping.tests.dto.PaymentConfirmRequestDto;
import com.camping.tests.dto.PaymentCreateRequestDto;

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

    public void 결제_생성_요청(PaymentCreateRequestDto request) {
        post(kioskHost(), "/api/payments", request);
    }

    public void 결제_확정_요청(PaymentConfirmRequestDto request) {
        post(kioskHost(), "/api/payments/confirm", request);
    }
}
