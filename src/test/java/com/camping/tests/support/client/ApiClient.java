package com.camping.tests.support.client;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public interface ApiClient {

    // Fluent API를 위한 Request Builder
    RequestBuilder get(String url);
    RequestBuilder post(String url);
    RequestBuilder put(String url);
    RequestBuilder patch(String url);
    RequestBuilder delete(String url);

    // Builder 인터페이스
    interface RequestBuilder {
        <T> RequestBuilder body(T body);
        RequestBuilder accessToken(String token);
        RequestBuilder needAuth(boolean needAuth);
        RequestBuilder needAuth(); // needAuth(true)의 편의 메서드
        ExtractableResponse<Response> execute();
    }

    // 기존 메서드들 (하위 호환성) - 매개변수 있는 오버로드
    <T> ExtractableResponse<Response> get(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> get(String url, T body);
    <T> ExtractableResponse<Response> get(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> post(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> post(String url, T body);
    <T> ExtractableResponse<Response> post(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> put(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> put(String url, T body);
    <T> ExtractableResponse<Response> put(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> patch(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> patch(String url, T body);
    <T> ExtractableResponse<Response> patch(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> delete(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> delete(String url, T body);
    <T> ExtractableResponse<Response> delete(String url, T body, boolean needAuthorization);

    // Directly 메서드들 - 내부 구현용
    <T> ExtractableResponse<Response> getDirectly(String url);
    <T> ExtractableResponse<Response> getDirectly(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> getDirectly(String url, T body);
    <T> ExtractableResponse<Response> getDirectly(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> postDirectly(String url);
    <T> ExtractableResponse<Response> postDirectly(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> postDirectly(String url, T body);
    <T> ExtractableResponse<Response> postDirectly(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> putDirectly(String url);
    <T> ExtractableResponse<Response> putDirectly(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> putDirectly(String url, T body);
    <T> ExtractableResponse<Response> putDirectly(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> patchDirectly(String url);
    <T> ExtractableResponse<Response> patchDirectly(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> patchDirectly(String url, T body);
    <T> ExtractableResponse<Response> patchDirectly(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> deleteDirectly(String url);
    <T> ExtractableResponse<Response> deleteDirectly(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> deleteDirectly(String url, T body);
    <T> ExtractableResponse<Response> deleteDirectly(String url, T body, boolean needAuthorization);
}