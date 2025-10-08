package com.camping.tests.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    private static final String URL = System.getProperty("camping.db.url", "jdbc:mysql://localhost:3307/camping?characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true");
    private static final String USER = System.getProperty("camping.db.username", "root");
    private static final String PASS = System.getProperty("camping.db.password", "root1234");
    private static final String SCHEMA = System.getProperty("camping.db.schema", "camping");

    public static void clearAll() {
        try {
            // 드라이버가 로드되었는지 확인 (JDBC 4+에서는 선택이지만 안전을 위해 수행)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignore) {
            // 드라이버가 클래스패스에 있으면 DriverManager로 여전히 동작함
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 외래 키 검사를 비활성화하여 어떤 순서로든 TRUNCATE 가능하도록 함
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            List<String> tables = fetchAllTables(conn, SCHEMA);
            System.out.println("[DatabaseCleaner] Found tables: " + tables);

            for (String table : tables) {
                if (shouldSkip(table)) {
                    continue;
                }
                try {
                    stmt.executeUpdate("TRUNCATE TABLE `" + table + "`");
                    System.out.println("[DatabaseCleaner] Truncated table: " + table);
                } catch (SQLException e) {
                    // 다른 시나리오에 영향 주지 않도록 로그만 남기고 계속 진행
                    System.err.println("[DatabaseCleaner] Failed to truncate table: " + table + ", reason: " + e.getMessage());
                }
            }

            // 외래 키 검사 다시 활성화
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            System.err.println("[DatabaseCleaner] DB clean failed: " + e.getMessage());
        }
    }

    public static void seedDefaultData() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignore) {}

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            try {
                // 참조 무결성 보장을 위해 INSERT 시 외래 키 검사 활성화 보장
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

                // Campsites
                stmt.executeUpdate(
                    "insert into campsites (id, site_number, description, max_people) values " +
                    "(1, 'A-01', '숲 뷰, 전기가능', 4)," +
                    "(2, 'A-02', '강가, 그늘많음', 6)"
                );

                // Products
                stmt.executeUpdate(
                    "insert into products (id, name, stock_quantity, price, product_type) values " +
                    "(1, '랜턴', 20, 30000.00, 'RENTAL')," +
                    "(2, '장작팩', 50, 10000.00, 'SALE')," +
                    "(3, '코펠 세트', 15, 20000.00, 'RENTAL')," +
                    "(4, '의자', 25, 15000.00, 'RENTAL')," +
                    "(5, '테이블', 10, 25000.00, 'RENTAL')," +
                    "(6, '버너', 12, 18000.00, 'RENTAL')," +
                    "(7, '취사도구 세트', 30, 12000.00, 'RENTAL')," +
                    "(8, '생수(2L)', 100, 2000.00, 'SALE')," +
                    "(9, '라면 세트', 80, 4000.00, 'SALE')," +
                    "(10, '스낵팩', 60, 3000.00, 'SALE')," +
                    "(11, '휴지', 70, 2500.00, 'SALE')," +
                    "(12, '아이스팩', 90, 1500.00, 'SALE')"
                );

                // Reservations
                stmt.executeUpdate(
                    "insert into reservations (customer_name, start_date, end_date, campsite_id, phone_number, status, reservation_date, confirmation_code, created_at) values " +
                    "('홍길동', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 1, '010-1111-2222', 'CONFIRMED', CURRENT_DATE, 'ABC123', CURRENT_TIMESTAMP)," +
                    "('김철수', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 2, '010-3333-4444', 'CONFIRMED', CURRENT_DATE, 'XYZ789', CURRENT_TIMESTAMP)," +
                    "('이영희', DATE_SUB(CURRENT_DATE, INTERVAL 28 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 27 DAY), 1, '010-5555-6666', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 29 DAY), 'R00003', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 29 DAY))," +
                    "('박민수', DATE_SUB(CURRENT_DATE, INTERVAL 25 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 24 DAY), 2, '010-7777-8888', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 26 DAY), 'R00004', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 26 DAY))," +
                    "('최수정', DATE_SUB(CURRENT_DATE, INTERVAL 21 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 19 DAY), 1, '010-9999-0000', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 22 DAY), 'R00005', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 22 DAY))," +
                    "('정하늘', DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 17 DAY), 2, '010-2222-3333', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 19 DAY), 'R00006', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 19 DAY))," +
                    "('오세훈', DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), 1, '010-4444-5555', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY), 'R00007', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 16 DAY))," +
                    "('유지민', DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 11 DAY), 2, '010-6666-7777', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 13 DAY), 'R00008', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY))," +
                    "('선우진', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), 1, '010-1212-3434', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), 'R00009', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY))," +
                    "('배수아', DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), 2, '010-5656-7878', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), 'R00010', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY))," +
                    "('고다빈', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), 1, '010-9090-1010', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), 'R00011', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY))," +
                    "('한도윤', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), CURRENT_DATE, 2, '010-2323-4545', 'CONFIRMED', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), 'R00012', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY))"
                );

                // Sales Records
                stmt.executeUpdate(
                    "insert into sales_records (id, product_id, quantity, total_price, created_at) values " +
                    "(1, 2, 3, 30000.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY))," +
                    "(2, 2, 1, 10000.00, CURRENT_TIMESTAMP)," +
                    "(3, 8, 5, 10000.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY))," +
                    "(4, 9, 2, 8000.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 DAY))," +
                    "(5, 10, 10, 30000.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 DAY))"
                );

                // Rental Records
                stmt.executeUpdate(
                    "insert into rental_records (id, reservation_id, product_id, quantity, is_returned, created_at) values " +
                    "(1, 3, 3, 2, false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY))," +
                    "(2, 4, 4, 1, true, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 DAY))," +
                    "(3, 5, 5, 3, false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 21 DAY))," +
                    "(4, 6, 6, 1, true, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 DAY))," +
                    "(5, 7, 7, 4, false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 DAY))," +
                    "(6, null, 3, 1, false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY))"
                );

                conn.commit();
                System.out.println("[DatabaseCleaner] Seed data inserted successfully");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[DatabaseCleaner] Seed data insertion failed: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseCleaner] DB connection failed during seed: " + e.getMessage());
        }
    }

    private static List<String> fetchAllTables(Connection conn, String schema) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='" + schema + "' AND table_type='BASE TABLE'";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }
        return tables;
    }

    private static boolean shouldSkip(String table) {
        if (table == null) return true;
        String t = table.toLowerCase();
        // 마이그레이션 또는 메타데이터용 공통 테이블은 건너뜀
        return t.contains("flyway") || t.equals("flyway_schema_history") || t.equals("schema_version");
    }
}
