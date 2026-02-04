package com.camping.tests.common;

public final class TestConstants {
    private TestConstants() {}

    public static final String DEFAULT_ORDER_ID = "dummy-order-id";
    public static final String DEFAULT_PAYMENT_KEY = "dummy-payment-key";

    public static final class Kiosk {
        private Kiosk() {}
        public static final String PRODUCTS_ENDPOINT = "/api/products";
        public static final String PAYMENTS_ENDPOINT = "/api/payments";
        public static final String PAYMENTS_CONFIRM_ENDPOINT = "/api/payments/confirm";
    }

    public static final class Payment {
        private Payment() {}
        public static final String PAYMENTS_ENDPOINT = "/v1/payments";
        public static final String PAYMENTS_CONFIRM_ENDPOINT = "/v1/payments/confirm";
    }
}
