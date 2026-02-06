package com.camping.tests.dto;

public record ReservationRequest(
        String siteNumber,
        String customerName,
        String startDate,
        String endDate
) {
    private static int callCounter = 0;

    public static ReservationRequest of(String siteNumber) {
        String customerName = "테스트고객_" + System.currentTimeMillis();
        int base = (int) (System.nanoTime() % 3000) + 100;
        int offset = base + (callCounter++ * 10);
        java.time.LocalDate start = java.time.LocalDate.now().plusDays(offset);
        java.time.LocalDate end = start.plusDays(2);
        return new ReservationRequest(siteNumber, customerName, start.toString(), end.toString());
    }
}
