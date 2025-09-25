package com.camping.tests.helper;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class RestAssuredHelper {

    private static final List<HttpMethodStrategy> strategies = List.of(
            new GetStrategy(),
            new PostStrategy(),
            new PutStrategy(),
            new PatchStrategy(),
            new DeleteStrategy()
    );

    public <T> ExtractableResponse<Response> execute(ServiceType serviceType, HttpMethod method, String url, T body, boolean needAuthorization) {
        RequestSpecification requestSpec = needAuthorization
                ? ServiceContext.getRequestSpecificationWithAccessToken(serviceType)
                : ServiceContext.getRequestSpecification(serviceType);

        for (HttpMethodStrategy strategy : strategies) {
            if (strategy.supports(method)) {
                return strategy.execute(requestSpec, url, body);
            }
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    public <T> ExtractableResponse<Response> execute(HttpMethod method, String url, T body, boolean needAuthorization) {
        return execute(ServiceType.KIOSK, method, url, body, needAuthorization);
    }

    // GET 메서드 편의 메서드들 (ServiceType 포함)
    public <T> ExtractableResponse<Response> get(ServiceType serviceType, String url, T body, boolean needAuthorization) {
        return execute(serviceType, HttpMethod.GET, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> get(ServiceType serviceType, String url, T body) {
        return get(serviceType, url, body, false);
    }

    public ExtractableResponse<Response> get(ServiceType serviceType, String url) {
        return get(serviceType, url, null, false);
    }

    public ExtractableResponse<Response> get(ServiceType serviceType, String url, boolean needAuthorization) {
        return get(serviceType, url, null, needAuthorization);
    }

    // GET 메서드 편의 메서드들 (기본값: KIOSK)
    public <T> ExtractableResponse<Response> get(String url, T body, boolean needAuthorization) {
        return get(ServiceType.KIOSK, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> get(String url, T body) {
        return get(url, body, false);
    }

    public ExtractableResponse<Response> get(String url) {
        return get(url, null, false);
    }

    public ExtractableResponse<Response> get(String url, boolean needAuthorization) {
        return get(url, null, needAuthorization);
    }

    // POST 메서드 편의 메서드들 (ServiceType 포함)
    public <T> ExtractableResponse<Response> post(ServiceType serviceType, String url, T body, boolean needAuthorization) {
        return execute(serviceType, HttpMethod.POST, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> post(ServiceType serviceType, String url, T body) {
        return post(serviceType, url, body, false);
    }

    public ExtractableResponse<Response> post(ServiceType serviceType, String url) {
        return post(serviceType, url, null, false);
    }

    public ExtractableResponse<Response> post(ServiceType serviceType, String url, boolean needAuthorization) {
        return post(serviceType, url, null, needAuthorization);
    }

    // POST 메서드 편의 메서드들 (기본값: KIOSK)
    public <T> ExtractableResponse<Response> post(String url, T body, boolean needAuthorization) {
        return post(ServiceType.KIOSK, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> post(String url, T body) {
        return post(url, body, false);
    }

    public ExtractableResponse<Response> post(String url) {
        return post(url, null, false);
    }

    public ExtractableResponse<Response> post(String url, boolean needAuthorization) {
        return post(url, null, needAuthorization);
    }

    // PUT 메서드 편의 메서드들 (ServiceType 포함)
    public <T> ExtractableResponse<Response> put(ServiceType serviceType, String url, T body, boolean needAuthorization) {
        return execute(serviceType, HttpMethod.PUT, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> put(ServiceType serviceType, String url, T body) {
        return put(serviceType, url, body, false);
    }

    public ExtractableResponse<Response> put(ServiceType serviceType, String url) {
        return put(serviceType, url, null, false);
    }

    public ExtractableResponse<Response> put(ServiceType serviceType, String url, boolean needAuthorization) {
        return put(serviceType, url, null, needAuthorization);
    }

    // PUT 메서드 편의 메서드들 (기본값: KIOSK)
    public <T> ExtractableResponse<Response> put(String url, T body, boolean needAuthorization) {
        return put(ServiceType.KIOSK, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> put(String url, T body) {
        return put(url, body, false);
    }

    public ExtractableResponse<Response> put(String url) {
        return put(url, null, false);
    }

    public ExtractableResponse<Response> put(String url, boolean needAuthorization) {
        return put(url, null, needAuthorization);
    }

    // PATCH 메서드 편의 메서드들 (ServiceType 포함)
    public <T> ExtractableResponse<Response> patch(ServiceType serviceType, String url, T body, boolean needAuthorization) {
        return execute(serviceType, HttpMethod.PATCH, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> patch(ServiceType serviceType, String url, T body) {
        return patch(serviceType, url, body, false);
    }

    public ExtractableResponse<Response> patch(ServiceType serviceType, String url) {
        return patch(serviceType, url, null, false);
    }

    public ExtractableResponse<Response> patch(ServiceType serviceType, String url, boolean needAuthorization) {
        return patch(serviceType, url, null, needAuthorization);
    }

    // PATCH 메서드 편의 메서드들 (기본값: KIOSK)
    public <T> ExtractableResponse<Response> patch(String url, T body, boolean needAuthorization) {
        return patch(ServiceType.KIOSK, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> patch(String url, T body) {
        return patch(url, body, false);
    }

    public ExtractableResponse<Response> patch(String url) {
        return patch(url, null, false);
    }

    public ExtractableResponse<Response> patch(String url, boolean needAuthorization) {
        return patch(url, null, needAuthorization);
    }

    // DELETE 메서드 편의 메서드들 (ServiceType 포함)
    public <T> ExtractableResponse<Response> delete(ServiceType serviceType, String url, T body, boolean needAuthorization) {
        return execute(serviceType, HttpMethod.DELETE, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> delete(ServiceType serviceType, String url, T body) {
        return delete(serviceType, url, body, false);
    }

    public ExtractableResponse<Response> delete(ServiceType serviceType, String url) {
        return delete(serviceType, url, null, false);
    }

    public ExtractableResponse<Response> delete(ServiceType serviceType, String url, boolean needAuthorization) {
        return delete(serviceType, url, null, needAuthorization);
    }

    // DELETE 메서드 편의 메서드들 (기본값: KIOSK)
    public <T> ExtractableResponse<Response> delete(String url, T body, boolean needAuthorization) {
        return delete(ServiceType.KIOSK, url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> delete(String url, T body) {
        return delete(url, body, false);
    }

    public ExtractableResponse<Response> delete(String url) {
        return delete(url, null, false);
    }

    public ExtractableResponse<Response> delete(String url, boolean needAuthorization) {
        return delete(url, null, needAuthorization);
    }
}