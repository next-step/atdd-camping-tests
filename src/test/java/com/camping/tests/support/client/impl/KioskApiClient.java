package com.camping.tests.support.client.impl;

import com.camping.tests.support.client.BaseApiClient;
import com.camping.tests.support.helper.ServiceType;

public class KioskApiClient extends BaseApiClient {

    public KioskApiClient() {
        super(ServiceType.KIOSK);
    }
}