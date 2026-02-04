package com.camping.tests.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
            return URI.create(getPaymentBaseUrl()).getHost();
        } catch (Exception e) {
            e.printStackTrace();
            return "localhost";
        }
    }

    public static int getPaymentMockPort() {
        try {
            URI uri = URI.create(getPaymentBaseUrl());
            int port = uri.getPort();

            if (port == -1) {
                return uri.toURL().getDefaultPort();
            }

            return port;
        } catch (Exception e) {
            e.printStackTrace();
            return 8084;
        }
    }
}
