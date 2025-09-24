package com.camping.tests.hooks;

import io.cucumber.java.Before;
import com.camping.tests.context.CommonContext;
import com.camping.tests.context.RequestSpecFactory;
import com.camping.tests.helpers.BaseApiHelper;

public class Hooks {

    @Before
    public void setUp() {
        CommonContext.setRequestSpec(RequestSpecFactory.create());
        String adminToken = BaseApiHelper.authenticateAndGetToken();
        CommonContext.setAdminToken(adminToken);
    }
}
