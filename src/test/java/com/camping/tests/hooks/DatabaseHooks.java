package com.camping.tests.hooks;

import io.cucumber.java.Before;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHooks {

    private static final String DB_URL = getEnvOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/atdd");
    private static final String DB_USER = getEnvOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = getEnvOrDefault("DB_PASSWORD", "secret");

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * E2E 테스트 실행 전 데이터베이스 초기화
     *
     * 실행 순서:
     * 1. 외래 키 제약 조건 비활성화
     * 2. 모든 테이블 데이터 삭제 (TRUNCATE)
     * 3. 외래 키 제약 조건 재활성화
     * 4. 테스트용 초기 데이터 삽입
     *
     * 주의: 테이블 생성은 각 서비스(Admin, Reservation)의 Hibernate ddl-auto 설정으로 처리됨
     */
    @Before("@e2e")
    public void resetDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = connection.createStatement();

            // 외래 키 제약 조건 임시 비활성화
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            // 모든 테이블 데이터 삭제
            stmt.executeUpdate("TRUNCATE TABLE products");
            stmt.executeUpdate("TRUNCATE TABLE campsites");
            stmt.executeUpdate("TRUNCATE TABLE sales_records");

            // 외래 키 제약 조건 임시 재활성화
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

            // 테스트용 초기 데이터 삽입
            stmt.executeUpdate("INSERT INTO products (id, name, price, product_type, stock_quantity) VALUES " +
                    "(1, '텐트', 50000, 'RENTAL', 10), " +
                    "(2, '침낭', 30000, 'RENTAL', 15), " +
                    "(3, '캠핑 의자', 25000, 'SALE', 20)");

            System.out.println("✓ Database reset and seeded");
        } catch (Exception e) {
            System.err.println("✗ Database reset failed: " + e.getMessage());
            throw e;
        }
    }
}
