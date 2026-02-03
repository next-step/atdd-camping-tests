package com.camping.tests.support;

public final class Endpoints {

    private Endpoints() {}

    public static final class Admin {
        public static final String LOGIN = "/auth/login";
        public static final String PRODUCTS = "/admin/products";
    }

    public static final class Kiosk {
        public static final String PRODUCTS = "/api/products";
        public static final String PAYMENTS = "/api/payments";
        public static final String PAYMENTS_CONFIRM = "/api/payments/confirm";
    }
}