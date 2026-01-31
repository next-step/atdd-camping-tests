package com.camping.tests.api;

import com.camping.tests.context.HttpContext;

import static com.camping.tests.config.ExternalAPIConfig.kioskHost;

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
}
