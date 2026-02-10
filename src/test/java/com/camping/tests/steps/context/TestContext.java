package com.camping.tests.steps.context;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ScenarioScope
public class TestContext {

    private String accessToken;
    private ExtractableResponse<Response> response;

    private final PaymentData payment = new PaymentData();
    private final ReservationData reservation = new ReservationData();
    private final ProductData product = new ProductData();
    private final RentalData rental = new RentalData();
    private final CustomerData customer = new CustomerData();

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public ExtractableResponse<Response> getResponse() {
        return response;
    }

    public void setResponse(ExtractableResponse<Response> response) {
        this.response = response;
    }

    public PaymentData getPayment() {
        return payment;
    }

    public ReservationData getReservation() {
        return reservation;
    }

    public ProductData getProduct() {
        return product;
    }

    public RentalData getRental() {
        return rental;
    }

    public CustomerData getCustomer() {
        return customer;
    }

    public static class PaymentData {
        private String paymentKey;
        private String orderId;
        private Map<String, Object> cartItemFixture;

        public String getPaymentKey() {
            return paymentKey;
        }

        public void setPaymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Map<String, Object> getCartItemFixture() {
            return cartItemFixture;
        }

        public void setCartItemFixture(Map<String, Object> cartItemFixture) {
            this.cartItemFixture = cartItemFixture;
        }
    }

    public static class ReservationData {
        private Long id;
        private String customerName;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ProductData {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class RentalData {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class CustomerData {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
