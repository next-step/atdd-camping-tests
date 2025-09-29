package com.camping.tests.steps.reservation.dto;

import java.time.LocalDate;

public record ReservationRequest(
    String customerName,
    LocalDate startDate,
    LocalDate endDate,
    String siteNumber,
    String phoneNumber,
    Integer numberOfPeople,
    String carNumber,
    String requests
) {
    public static Builder fixture() {
        return new Builder()
            .customerName("홍길동")
            .phoneNumber("010-1234-5678")
            .numberOfPeople(2)
            .carNumber("12가3456")
            .requests("조용한 곳으로 부탁드립니다");
    }

    public static class Builder {
        private String customerName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String siteNumber;
        private String phoneNumber;
        private Integer numberOfPeople;
        private String carNumber;
        private String requests;

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder siteNumber(String siteNumber) {
            this.siteNumber = siteNumber;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder numberOfPeople(Integer numberOfPeople) {
            this.numberOfPeople = numberOfPeople;
            return this;
        }

        public Builder carNumber(String carNumber) {
            this.carNumber = carNumber;
            return this;
        }

        public Builder requests(String requests) {
            this.requests = requests;
            return this;
        }

        public ReservationRequest create() {
            return new ReservationRequest(
                customerName,
                startDate,
                endDate,
                siteNumber,
                phoneNumber,
                numberOfPeople,
                carNumber,
                requests
            );
        }
    }
}