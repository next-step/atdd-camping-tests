package com.camping.tests.api;

import com.camping.tests.context.AdminContext;
import com.camping.tests.context.HttpContext;

import java.util.HashMap;
import java.util.Map;

import static com.camping.tests.config.ExternalAPIConfig.*;

@SuppressWarnings("NonAsciiCharacters")
public class AdminApi extends BasicApi {

    private final AdminContext adminContext;

    public AdminApi(HttpContext httpContext, AdminContext adminContext) {
        super(httpContext);
        this.adminContext = adminContext;
    }

    public void 로그인_요청() {
        String username = getAdminUsername();
        String password = getAdminPassword();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        post(adminHost(), "/auth/login", requestBody);
        String token = httpContext.getResponse().jsonPath().getString("accessToken");
        adminContext.setAdminToken(token);
    }

    public void 헬스_체크(String token) {
        get(adminHost(), "/", Map.of("Authorization", "Bearer " + token));
    }
}
