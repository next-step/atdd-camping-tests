package com.camping.tests.support.client.impl;

import com.camping.tests.support.client.BaseApiClient;
import com.camping.tests.support.helper.ServiceType;

public class ReservationApiClient extends BaseApiClient {

    public ReservationApiClient() {
        super(ServiceType.RESERVATION);
    }
}