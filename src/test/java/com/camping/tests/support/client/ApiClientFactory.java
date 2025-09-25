package com.camping.tests.support.client;

import com.camping.tests.support.client.impl.AdminApiClient;
import com.camping.tests.support.client.impl.KioskApiClient;
import com.camping.tests.support.client.impl.ReservationApiClient;
import com.camping.tests.support.helper.ServiceType;

public class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static ApiClient create(ServiceType serviceType) {
        return switch (serviceType) {
            case KIOSK -> new KioskApiClient();
            case ADMIN -> new AdminApiClient();
            case RESERVATION -> new ReservationApiClient();
            default -> throw new IllegalArgumentException("Unsupported service type: " + serviceType);
        };
    }

    public static ApiClient kiosk() {
        return create(ServiceType.KIOSK);
    }

    public static ApiClient admin() {
        return create(ServiceType.ADMIN);
    }

    public static ApiClient reservation() {
        return create(ServiceType.RESERVATION);
    }
}