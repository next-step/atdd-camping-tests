package com.camping.tests.steps.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long id,
    String customerName,
    LocalDate startDate,
    LocalDate endDate,
    String siteNumber,
    String phoneNumber,
    String status,
    String confirmationCode,
    LocalDateTime createdAt
) {
}