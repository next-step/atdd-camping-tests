package com.camping.tests.support;

public final class Endpoints {

    private Endpoints() {}

    public static final class Admin {
        public static final String LOGIN = "/auth/login";
        public static final String PRODUCTS = "/admin/products";
        public static final String SALES = "/api/sales";
        public static final String RESERVATIONS = "/admin/reservations";
        public static final String RESERVATION_STATUS = "/admin/reservations/%d/status";
        public static final String RESERVATION_LOOKUP = "/admin/reservations/lookup";
    }

    public static final class Kiosk {
        public static final String PRODUCTS = "/api/products";
        public static final String PAYMENTS = "/api/payments";
        public static final String PAYMENTS_CONFIRM = "/api/payments/confirm";
    }

    public static final class Reservation {
        public static final String BASE = "/api/reservations";
        public static final String BY_ID = "/api/reservations/%d";
    }
}