package com.camping.tests.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = CucumberSpringConfiguration.CucumberTestConfig.class)
public class CucumberSpringConfiguration {

    @Configuration
    @ComponentScan(basePackages = "com.camping.tests")
    static class CucumberTestConfig {
    }
}
