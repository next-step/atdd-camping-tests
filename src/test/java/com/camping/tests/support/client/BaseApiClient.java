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

    protected <T> ExtractableResponse<Response> execute(HttpMethod method, String url, T body, boolean needAuthorization) {
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

    @Override
    public <T> ExtractableResponse<Response> get(String url) {
        return execute(HttpMethod.GET, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> get(String url, boolean needAuthorization) {
        return execute(HttpMethod.GET, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> get(String url, T body) {
        return execute(HttpMethod.GET, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> get(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.GET, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> post(String url) {
        return execute(HttpMethod.POST, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> post(String url, boolean needAuthorization) {
        return execute(HttpMethod.POST, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> post(String url, T body) {
        return execute(HttpMethod.POST, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> post(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.POST, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> put(String url) {
        return execute(HttpMethod.PUT, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> put(String url, boolean needAuthorization) {
        return execute(HttpMethod.PUT, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> put(String url, T body) {
        return execute(HttpMethod.PUT, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> put(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.PUT, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> patch(String url) {
        return execute(HttpMethod.PATCH, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> patch(String url, boolean needAuthorization) {
        return execute(HttpMethod.PATCH, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> patch(String url, T body) {
        return execute(HttpMethod.PATCH, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> patch(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.PATCH, url, body, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> delete(String url) {
        return execute(HttpMethod.DELETE, url, null, false);
    }

    @Override
    public <T> ExtractableResponse<Response> delete(String url, boolean needAuthorization) {
        return execute(HttpMethod.DELETE, url, null, needAuthorization);
    }

    @Override
    public <T> ExtractableResponse<Response> delete(String url, T body) {
        return execute(HttpMethod.DELETE, url, body, false);
    }

    @Override
    public <T> ExtractableResponse<Response> delete(String url, T body, boolean needAuthorization) {
        return execute(HttpMethod.DELETE, url, body, needAuthorization);
    }
}