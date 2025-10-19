package com.camping.tests;

import io.restassured.response.Response;

public class CommonContext {
    public static Response lastResponse = null;
    public static String orderId = null;
    public static String paymentKey = null;
    public static String isSuccess = null;
    public static String responseMessage = null;

    public static void clear() {
        lastResponse = null;
        orderId = null;
        paymentKey = null;
        isSuccess = null;
        responseMessage = null;
    }
}
