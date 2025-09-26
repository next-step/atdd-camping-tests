package com.camping.tests.support.client.impl;

import com.camping.tests.support.client.BaseApiClient;
import com.camping.tests.support.helper.ServiceType;

public class AdminApiClient extends BaseApiClient {

    public AdminApiClient() {
        super(ServiceType.ADMIN);
    }
}