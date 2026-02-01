# 상품 목록 조회 API 분석 보고서

## 1. API 개요
- **엔드포인트:** `/api/products`
- **HTTP 메서드:** `GET`
- **클래스:** `com.camping.kiosk.web.ProductController`
- **설명:** 키오스크 화면에 표시할 상품 목록을 외부 어드민 시스템으로부터 조회하여 반환합니다.

## 2. 응답 데이터 구조 (`Product`)
JSON 배열 형태로 반환되며, 각 항목은 아래 필드를 포함합니다.
- `id` (Long): 상품 ID
- `name` (String): 상품명
- `price` (int): 상품 가격
- `stockQuantity` (int): 재고 수량
- `productType` (String): 상품 유형

## 3. 내부 로직 흐름
1. **Controller Layer** (`ProductController`):
   - `/api/products` 요청을 수신합니다.
   - `AdminService.loadProducts()`를 호출합니다.
   - 결과를 `ResponseEntity.ok()`로 감싸서 200 OK 상태로 반환합니다.

2. **Service Layer** (`AdminService`):
   - 비즈니스 로직 없이 `AdminClient`에게 위임하는 역할만 수행합니다.
   - `AdminClient.getProducts()`를 호출합니다.

3. **External Integration Layer** (`AdminClient`):
   - `RestTemplate`을 사용하여 외부 어드민 시스템과 통신합니다.
   - **타겟 URL:** `${kiosk.admin.base-url}/admin/products`
   - **인증 처리:**
     - `AdminAuthClient`를 통해 인증 헤더(Cookie 등)를 가져옵니다.
   - **재시도 메커니즘:**
     - 1차 요청 실패 시, 인증 토큰(캐시)을 초기화(`clearCache()`)하고 재인증 후 1회 재시도합니다.

## 4. 특이 사항
- 어드민 시스템 의존성이 강하며, 어드민 시스템 장애 시 해당 API도 실패합니다.
- 재시도 로직이 `try-catch`로 하드코딩 되어 있어, 모든 예외에 대해 재시도를 수행합니다.
