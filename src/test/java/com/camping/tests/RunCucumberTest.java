package com.camping.tests;

import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource("features")
@SelectPackages("com.camping.tests.steps")
public class RunCucumberTest {
}
