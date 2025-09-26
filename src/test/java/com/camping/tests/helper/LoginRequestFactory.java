package com.camping.tests.helper;

import java.util.HashMap;
import java.util.Map;

public class LoginRequestFactory {

    public static Map<String, Object> getLoginRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("username", "admin");
        request.put("password", "admin123");
        return request;
    }
}
