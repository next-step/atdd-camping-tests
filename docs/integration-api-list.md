# 🔗 통합 시나리오 API 목록

## 📋 개요

통합 시나리오에서 사용되는 모든 API 엔드포인트 목록입니다. Swagger 어노테이션 추가 및 OpenAPI 명세 작성의 기준이 됩니다.

---

## 🛠️ Admin Service (포트: 18082)

### 인증 API

| Method | Endpoint      | 용도          | 인증 필요 |
|--------|---------------|-------------|-------|
| POST   | `/auth/login` | 관리자/키오스크 인증 | ❌     |

### 상품 관리 API

| Method | Endpoint                      | 용도       | 인증 필요 |
|--------|-------------------------------|----------|-------|
| GET    | `/admin/products`             | 상품 목록 조회 | ✅     |
| POST   | `/admin/products`             | 상품 생성    | ✅     |
| PUT    | `/admin/products/{productId}` | 상품 수정    | ✅     |

### 예약 관리 API

| Method | Endpoint                                     | 용도       | 인증 필요 |
|--------|----------------------------------------------|----------|-------|
| GET    | `/admin/reservations`                        | 예약 목록 조회 | ✅     |
| PATCH  | `/admin/reservations/{reservationId}/status` | 예약 상태 변경 | ✅     |

### 캠프사이트 관리 API

| Method | Endpoint           | 용도        | 인증 필요 |
|--------|--------------------|-----------|-------|
| GET    | `/admin/campsites` | 사이트 목록 조회 | ✅     |
| POST   | `/admin/campsites` | 사이트 생성    | ✅     |

### 매출 관리 API

| Method | Endpoint     | 용도       | 인증 필요 |
|--------|--------------|----------|-------|
| POST   | `/api/sales` | 매출 기록 생성 | ✅     |
| GET    | `/api/sales` | 매출 기록 조회 | ✅     |

---

## 🏪 Kiosk Service (포트: 18081)

### 상품 조회 API

| Method | Endpoint        | 용도             | 인증 필요 |
|--------|-----------------|----------------|-------|
| GET    | `/api/products` | 상품 목록 조회 (고객용) | ❌     |

### 결제 처리 API

| Method | Endpoint                | 용도       | 인증 필요 |
|--------|-------------------------|----------|-------|
| POST   | `/api/payments`         | 결제 세션 생성 | ❌     |
| POST   | `/api/payments/confirm` | 결제 확인    | ❌     |

---

## 🏕️ Reservation Service (포트: 18083)

### 예약 관리 API

| Method | Endpoint                     | 용도        | 인증 필요    |
|--------|------------------------------|-----------|----------|
| POST   | `/api/reservations`          | 예약 생성     | ❌        |
| GET    | `/api/reservations`          | 예약 목록 조회  | ❌        |
| GET    | `/api/reservations/{id}`     | 예약 상세 조회  | ❌        |
| PUT    | `/api/reservations/{id}`     | 예약 수정     | ✅ (확인코드) |
| DELETE | `/api/reservations/{id}`     | 예약 취소     | ✅ (확인코드) |
| GET    | `/api/reservations/calendar` | 예약 캘린더 조회 | ❌        |

### 사이트 가용성 API

| Method | Endpoint                               | 용도            | 인증 필요 |
|--------|----------------------------------------|---------------|-------|
| GET    | `/api/sites`                           | 사이트 목록 조회     | ❌     |
| GET    | `/api/sites/{siteNumber}/availability` | 특정 사이트 가용성 확인 | ❌     |
| GET    | `/api/sites/available`                 | 가용한 사이트 조회    | ❌     |

---

## 📝 상세 API 명세

### Admin Service

#### POST /auth/login

```json
Request:
{
"username": "admin",
"password": "admin123"
}

Response: {
"accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"tokenType": "Bearer",
"expiresIn": 3600
}
```

#### GET /admin/products

```json
Response:
[
{
"id": 1,
"name": "캠핑 텐트",
"price": 50000,
"stockQuantity": 10,
"productType": "CAMPING",
"description": "2인용 캠핑 텐트",
"createdAt": "2024-12-20T10:00:00",
"updatedAt": "2024-12-20T10:00:00"
}
]
```

#### POST /admin/products

```json
Request:
{
"name": "캠핑 텐트",
"price": 50000,
"stockQuantity": 10,
"productType": "CAMPING",
"description": "2인용 캠핑 텐트"
}

Response: {
"id": 1,
"name": "캠핑 텐트",
"price": 50000,
"stockQuantity": 10,
"productType": "CAMPING",
"description": "2인용 캠핑 텐트",
"createdAt": "2024-12-20T10:00:00",
"updatedAt": "2024-12-20T10:00:00"
}
```

#### PUT /admin/products/{productId}

```json
Request:
{
"name": "슬리핑백",
"price": 35000,
"stockQuantity": 8,
"description": "겨울용 슬리핑백"
}

Response: {
"id": 1,
"name": "슬리핑백",
"price": 35000,
"stockQuantity": 8,
"productType": "CAMPING",
"description": "겨울용 슬리핑백",
"createdAt": "2024-12-20T10:00:00",
"updatedAt": "2024-12-20T11:00:00"
}
```

#### GET /admin/reservations

```json
Response:
[
{
"id": 1,
"reservationCode": "R001",
"customerName": "홍길동",
"phone": "010-1234-5678",
"siteName": "A구역-01",
"checkIn": "2024-12-25",
"checkOut": "2024-12-27",
"guestCount": 3,
"status": "CONFIRMED",
"totalAmount": 50000,
"createdAt": "2024-12-20T10:00:00"
}
]
```

#### PATCH /admin/reservations/{reservationId}/status

```json
Request:
{
"status": "CHECKED_IN"
}

Response: {
"id": 1,
"status": "CHECKED_IN",
"updatedAt": "2024-12-25T15:00:00"
}
```

### Kiosk Service

#### GET /api/products

```json
Response:
[
{
"id": 1,
"name": "캠핑 텐트",
"price": 50000,
"stockQuantity": 10,
"productType": "CAMPING",
"description": "2인용 캠핑 텐트",
"imageUrl": "/images/tent.jpg"
}
]
```

#### POST /api/payments

```json
Request:
{
"items": [
{
"productId": 1,
"productName": "캠핑 텐트",
"unitPrice": 50000,
"quantity": 2
}
],
"paymentMethod": "CARD",
"totalAmount": 100000
}

Response: {
"paymentId": "PAY_001",
"paymentKey": "payment_12345abcde",
"status": "PENDING",
"totalAmount": 100000,
"createdAt": "2024-12-20T10:00:00"
}
```

#### POST /api/payments/confirm

```json
Request:
{
"paymentKey": "payment_12345abcde",
"orderId": "ORDER_001",
"amount": 100000
}

Response: {
"success": true,
"paymentKey": "payment_12345abcde",
"orderId": "ORDER_001",
"status": "CONFIRMED",
"confirmedAt": "2024-12-20T10:05:00"
}
```

### Reservation Service

#### POST /api/reservations

```json
Request:
{
"siteName": "A구역-01",
"checkIn": "2024-12-25",
"checkOut": "2024-12-27",
"guestCount": 3,
"customerName": "홍길동",
"phone": "010-1234-5678",
"email": "hong@example.com"
}

Response: {
"id": 1,
"reservationCode": "R001",
"confirmationCode": "CONF123",
"customerName": "홍길동",
"phone": "010-1234-5678",
"siteName": "A구역-01",
"checkIn": "2024-12-25",
"checkOut": "2024-12-27",
"guestCount": 3,
"status": "CONFIRMED",
"totalAmount": 50000,
"createdAt": "2024-12-20T10:00:00"
}
```

#### GET /api/reservations/calendar

```json
Request Parameters:
?year=2024&month=12&siteId=1

Response: {
"year": 2024,
"month": 12,
"siteId": 1,
"siteName": "A구역-01",
"calendar": [
{
"date": "2024-12-25",
"available": false,
"reservationId": 1,
"customerName": "홍길동"
},
{
"date": "2024-12-26",
"available": false,
"reservationId": 1,
"customerName": "홍길동"
},
{
"date": "2024-12-27",
"available": true
}
]
}
```

#### GET /api/sites/available

```json
Request Parameters:
?date=2024-12-25

Response:
[
{
"siteNumber": "B구역-01",
"siteName": "B구역-01",
"maxPeople": 4,
"pricePerNight": 25000,
"amenities": ["화장실", "샤워실"],
"available": true
}
]
```

---

## 🔐 인증 방식

### JWT Bearer Token

- Admin 서비스의 모든 `/admin/*` 엔드포인트
- Header: `Authorization: Bearer {token}`

### Confirmation Code

- Reservation 서비스의 수정/삭제 작업
- Query Parameter: `?confirmationCode={code}`

---

## ⚠️ 오류 응답 형식

```json
{
  "success": false,
  "errorCode": "INVALID_REQUEST",
  "message": "요청 데이터가 올바르지 않습니다",
  "details": "필수 필드가 누락되었습니다: customerName",
  "timestamp": "2024-12-20T10:00:00"
}
```

---

## 📊 통합 패턴

1. **Kiosk → Admin**: 상품 조회 및 매출 기록
2. **Admin → Reservation**: 예약 관리 및 상태 변경
3. **인증 플로우**: JWT 토큰 기반 인증
4. **재고 동기화**: 실시간 재고 업데이트
5. **오류 처리**: 일관된 오류 응답 형식

이 API 목록을 기반으로 Swagger 어노테이션을 추가하고 OpenAPI 명세를 작성합니다.