package com.camping.tests.steps;

import com.camping.tests.clients.AdminClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.ko.만약;

public class AdminSteps {
    private final ScenarioContext context;
    private final AdminClient adminClient;

    public AdminSteps(ScenarioContext context) {
        this.context = context;
        this.adminClient = new AdminClient();
    }

    @만약("관리자 서비스의 {string}에 GET 요청을 보낸다")
    public void 관리자_서비스의_GET_요청을_보낸다(String path) {
        var response = adminClient.getFromAdmin(path);
        context.setResponse(response);
    }
}
