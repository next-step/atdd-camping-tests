package com.camping.tests.support.client;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public interface ApiClient {

    <T> ExtractableResponse<Response> get(String url);
    <T> ExtractableResponse<Response> get(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> get(String url, T body);
    <T> ExtractableResponse<Response> get(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> post(String url);
    <T> ExtractableResponse<Response> post(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> post(String url, T body);
    <T> ExtractableResponse<Response> post(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> put(String url);
    <T> ExtractableResponse<Response> put(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> put(String url, T body);
    <T> ExtractableResponse<Response> put(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> patch(String url);
    <T> ExtractableResponse<Response> patch(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> patch(String url, T body);
    <T> ExtractableResponse<Response> patch(String url, T body, boolean needAuthorization);

    <T> ExtractableResponse<Response> delete(String url);
    <T> ExtractableResponse<Response> delete(String url, boolean needAuthorization);
    <T> ExtractableResponse<Response> delete(String url, T body);
    <T> ExtractableResponse<Response> delete(String url, T body, boolean needAuthorization);
}