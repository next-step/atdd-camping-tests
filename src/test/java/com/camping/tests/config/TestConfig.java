package com.camping.tests.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class TestConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    public static String getKioskBaseUrl() {
        return getProperty("kiosk.base.url");
    }

    public static String getAdminBaseUrl() {
        return getProperty("admin.base.url");
    }

    public static String getReservationBaseUrl() {
        return getProperty("reservation.base.url");
    }

    public static String getPaymentBaseUrl() {
        return getProperty("payment.base.url");
    }

    public static String getPaymentMockHost() {
        try {
            URL url = new URL(getPaymentBaseUrl());
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "localhost";
        }
    }

    public static int getPaymentMockPort() {
        try {
            URL url = new URL(getPaymentBaseUrl());
            int port = url.getPort();
            return port == -1 ? url.getDefaultPort() : port;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 8084;
        }
    }
}
