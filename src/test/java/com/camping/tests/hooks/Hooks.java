package com.camping.tests.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.context.RequestSpecFactory;
import com.camping.tests.helpers.BaseApiHelper;

public class Hooks {

    @Before
    public void setUp() {
        CommonContextHolder context = CommonContextHolder.getInstance();
        context.setRequestSpec(RequestSpecFactory.create());
        String adminToken = BaseApiHelper.authenticateAndGetToken();
        context.setAdminToken(adminToken);
    }

    @After
    public void tearDown() {
        CommonContextHolder.clear();
    }
}
