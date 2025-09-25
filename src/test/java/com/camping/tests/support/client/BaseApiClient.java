package com.camping.tests.support.client;

import com.camping.tests.support.helper.*;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public abstract class BaseApiClient implements ApiClient {

    private final ServiceType serviceType;
    private final List<HttpMethodStrategy> strategies = List.of(
            new GetStrategy(),
            new PostStrategy(),
            new PutStrategy(),
            new PatchStrategy(),
            new DeleteStrategy()
    );

    protected BaseApiClient(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    // Fluent API 구현
    @Override
    public RequestBuilder get(String url) {
        return new RequestBuilderImpl(HttpMethod.GET, url);
    }

    @Override
    public RequestBuilder post(String url) {
        return new RequestBuilderImpl(HttpMethod.POST, url);
    }

    @Override
    public RequestBuilder put(String url) {
        return new RequestBuilderImpl(HttpMethod.PUT, url);
    }

    @Override
    public RequestBuilder patch(String url) {
        return new RequestBuilderImpl(HttpMethod.PATCH, url);
    }

    @Override
    public RequestBuilder delete(String url) {
        return new RequestBuilderImpl(HttpMethod.DELETE, url);
    }

    // RequestBuilder 구현체
    private class RequestBuilderImpl implements RequestBuilder {
        private final HttpMethod method;
        private final String url;
        private Object body;
        private String customAccessToken;
        private boolean needAuth = false;

        public RequestBuilderImpl(HttpMethod method, String url) {
            this.method = method;
            this.url = url;
        }

        @Override
        public <T> RequestBuilder body(T body) {
            this.body = body;
            return this;
        }

        @Override
        public RequestBuilder accessToken(String token) {
            this.customAccessToken = token;
            this.needAuth = true; // accessToken 설정 시 자동으로 인증 필요로 설정
            return this;
        }

        @Override
        public RequestBuilder needAuth(boolean needAuth) {
            this.needAuth = needAuth;
            return this;
        }

        @Override
        public RequestBuilder needAuth() {
            this.needAuth = true;
            return this;
        }

        @Override
        public ExtractableResponse<Response> execute() {
            return executeWithCustomToken(method, url, body, needAuth, customAccessToken);
        }
    }

    protected <T> ExtractableResponse<Response> execute(HttpMethod method, String url, T body, boolean needAuthorization) {
        return executeWithCustomToken(method, url, body, needAuthorization, null);
    }

    protected <T> ExtractableResponse<Response> executeWithCustomToken(HttpMethod method, String url, T body, boolean needAuthorization, String customToken) {
        RequestSpecification requestSpec;

        if (customToken != null) {
            // 커스텀 토큰 사용
            requestSpec = ServiceContext.getRequestSpecification(serviceType)
                    .header("Authorization", "Bearer " + customToken);
        } else if (needAuthorization) {
            // 기본 토큰 사용
            requestSpec = ServiceContext.getRequestSpecificationWithAccessToken(serviceType);
        } else {
            // 인증 없음
            requestSpec = ServiceContext.getRequestSpecification(serviceType);
        }

        for (HttpMethodStrategy strategy : strategies) {
            if (strategy.supports(method)) {
                return strategy.execute(requestSpec, url, body);
            }
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    // 기존 메서드들 (하위 호환성을 위해 유지)
    // get, post, put, patch, delete 메서드들 - 기존 사용법 지원
    public <T> ExtractableResponse<Response> get(String url) {
        return getDirectly(url);
    }

    public <T> ExtractableResponse<Response> get(String url, boolean needAuthorization) {
        return getDirectly(url, needAuthorization);
    }

    public <T> ExtractableResponse<Response> get(String url, T body) {
        return getDirectly(url, body);
    }

    public <T> ExtractableResponse<Response> get(String url, T body, boolean needAuthorization) {
        return getDirectly(url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> post(String url) {
        return postDirectly(url);
    }

    public <T> ExtractableResponse<Response> post(String url, boolean needAuthorization) {
        return postDirectly(url, needAuthorization);
    }

    public <T> ExtractableResponse<Response> post(String url, T body) {
        return postDirectly(url, body);
    }

    public <T> ExtractableResponse<Response> post(String url, T body, boolean needAuthorization) {
        return postDirectly(url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> put(String url) {
        return putDirectly(url);
    }

    public <T> ExtractableResponse<Response> put(String url, boolean needAuthorization) {
        return putDirectly(url, needAuthorization);
    }

    public <T> ExtractableResponse<Response> put(String url, T body) {
        return putDirectly(url, body);
    }

    public <T> ExtractableResponse<Response> put(String url, T body, boolean needAuthorization) {
        return putDirectly(url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> patch(String url) {
        return patchDirectly(url);
    }

    public <T> ExtractableResponse<Response> patch(String url, boolean needAuthorization) {
        return patchDirectly(url, needAuthorization);
    }

    public <T> ExtractableResponse<Response> patch(String url, T body) {
        return patchDirectly(url, body);
    }

    public <T> ExtractableResponse<Response> patch(String url, T body, boolean needAuthorization) {
        return patchDirectly(url, body, needAuthorization);
    }

    public <T> ExtractableResponse<Response> delete(String url) {
        return deleteDirectly(url);
    }

    public <T> ExtractableResponse<Response> delete(String url, boolean needAuthorization) {
        return deleteDirectly(url, needAuthorization);
    }

    public <T> ExtractableResponse<Response> delete(String url, T body) {
        return deleteDirectly(url, body);
    }

    public <T> ExtractableResponse<Response> delete(String url, T body, boolean needAuthorization) {
        return deleteDirectly(url, body, needAuthorization);
    }

    // Directly 메서드들 - 내부 구현용
    @Override
    public <T> ExtractableResponse<Response> getDirectly(String url) {
        return execute(HttpMethod.GET, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> getDirectly(String url, boolean needAuthorization) {
        return execute(HttpMethod.GET, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> getDirectly(String url, T body) {
        return execute(HttpMethod.GET, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> getDirectly(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.GET, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> postDirectly(String url) {
        return execute(HttpMethod.POST, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> postDirectly(String url, boolean needAuthorization) {
        return execute(HttpMethod.POST, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> postDirectly(String url, T body) {
        return execute(HttpMethod.POST, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> postDirectly(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.POST, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> putDirectly(String url) {
        return execute(HttpMethod.PUT, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> putDirectly(String url, boolean needAuthorization) {
        return execute(HttpMethod.PUT, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> putDirectly(String url, T body) {
        return execute(HttpMethod.PUT, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> putDirectly(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.PUT, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> patchDirectly(String url) {
        return execute(HttpMethod.PATCH, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> patchDirectly(String url, boolean needAuthorization) {
        return execute(HttpMethod.PATCH, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> patchDirectly(String url, T body) {
        return execute(HttpMethod.PATCH, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> patchDirectly(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.PATCH, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> deleteDirectly(String url) {
        return execute(HttpMethod.DELETE, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> deleteDirectly(String url, boolean needAuthorization) {
        return execute(HttpMethod.DELETE, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> deleteDirectly(String url, T body) {
        return execute(HttpMethod.DELETE, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> deleteDirectly(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.DELETE, url, body, needAuthorization);
    }
}