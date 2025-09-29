package com.camping.tests.steps.reservation;

import com.camping.tests.utils.DatabaseHelper;

import java.sql.SQLException;

public class CampsiteTestSteps {

    public static void 캠프사이트를_생성한다(String siteNumber) throws SQLException {
        캠프사이트를_생성한다(siteNumber, siteNumber + " 번 캠프사이트", 4);
    }

    public static void 캠프사이트를_생성한다(String siteNumber, String description, Integer maxPeople) throws SQLException {
        String insertQuery = "INSERT INTO campsites (site_number, description, max_people) VALUES (?, ?, ?)";

        try (var connection = DatabaseHelper.getConnection();
             var preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, siteNumber);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, maxPeople);
            preparedStatement.executeUpdate();
        }
    }
}