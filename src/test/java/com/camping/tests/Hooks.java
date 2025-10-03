package com.camping.tests;

import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void beforeScenario() {
        ContextHelper.clearContext();
    }

}
