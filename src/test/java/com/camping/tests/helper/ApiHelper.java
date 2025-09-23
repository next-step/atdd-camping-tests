package com.camping.tests.helper;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class ApiHelper {

    private static final RestAssuredHelper restAssuredHelper = new RestAssuredHelper();

    // 기본 요청 (인증 없음)
    public static <T> ExtractableResponse<Response> createExtractableResponse(String httpMethod, String url, T body) {
        return createExtractableResponse(httpMethod, url, body, false);
    }

    public static ExtractableResponse<Response> createExtractableResponse(String httpMethod, String url) {
        return createExtractableResponse(httpMethod, url, null, false);
    }

    // 인증 필요 요청
    public static <T> ExtractableResponse<Response> createExtractableResponseWithAuthorization(String httpMethod, String url, T body) {
        return createExtractableResponse(httpMethod, url, body, true);
    }

    public static ExtractableResponse<Response> createExtractableResponseWithAuthorization(String httpMethod, String url) {
        return createExtractableResponse(httpMethod, url, null, true);
    }

    // 세밀한 제어 (직접 사용 권장하지 않음)
    public static <T> ExtractableResponse<Response> createExtractableResponse(String httpMethod, String url, T body, boolean needAuthorization) {
        HttpMethod method = HttpMethod.valueOf(httpMethod.toUpperCase());
        return restAssuredHelper.execute(method, url, body, needAuthorization);
    }
}