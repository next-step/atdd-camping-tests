package com.camping.tests.steps.reservation.dto;

public record CampsiteRequest(
    String siteNumber,
    String description,
    Integer maxPeople
) {
    public static CampsiteRequest fixture(String siteNumber) {
        return new CampsiteRequest(
            siteNumber,
            siteNumber + " 번 캠프사이트",
            4
        );
    }
}