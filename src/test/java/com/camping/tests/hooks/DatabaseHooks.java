package com.camping.tests.hooks;

import com.camping.tests.config.TestConfig;
import io.cucumber.java.Before;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHooks {

    @Before("@api")
    public void resetDatabase() {
        try (Connection conn = DriverManager.getConnection(
                TestConfig.getDbUrl(),
                TestConfig.getDbUsername(),
                TestConfig.getDbPassword())) {

            Statement stmt = conn.createStatement();

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 모든 테이블 TRUNCATE
            List<String> tables = getAllTables(conn);
            for (String table : tables) {
                stmt.execute("TRUNCATE TABLE " + table);
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            // 공통 데이터 로드 (campsites 등)
            executeSeedSql(conn);

        } catch (SQLException e) {
            throw new RuntimeException("DB 초기화 실패", e);
        }
    }

    private List<String> getAllTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }

        return tables;
    }

    private void executeSeedSql(Connection conn) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("seed.sql")) {
            if (is == null) {
                return; // seed.sql 없으면 스킵
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Statement stmt = conn.createStatement();

            // 세미콜론으로 분리해서 실행
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

        } catch (IOException | SQLException e) {
            throw new RuntimeException("seed.sql 실행 실패", e);
        }
    }
}