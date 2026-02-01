-- 백업: 개발/데모용 초기 데이터
-- Campsites
INSERT INTO campsites (site_number, description, max_people) VALUES
     ('A-1', '대형 사이트 - 전기 있음', 6),
     ('A-2', '대형 사이트 - 전기 있음', 6),
     ('A-3', '대형 사이트 - 전기 있음', 6),
     ('B-1', '소형 사이트 - 전기 있음', 4),
     ('B-2', '소형 사이트 - 전기 있음', 4),
     ('B-3', '소형 사이트 - 전기 있음', 4);
-- Products
INSERT INTO products (name, stock_quantity, price, product_type) VALUES
    ('랜턴', 20, 30000.00, 'RENTAL'),
    ('장작팩', 50, 10000.00, 'SALE'),
    ('코펠 세트', 15, 20000.00, 'RENTAL'),
    ('의자', 25, 15000.00, 'RENTAL'),
    ('테이블', 10, 25000.00, 'RENTAL'),
    ('버너', 12, 18000.00, 'RENTAL'),
    ('취사도구 세트', 30, 12000.00, 'RENTAL'),
    ('생수(2L)', 100, 2000.00, 'SALE'),
    ('라면 세트', 80, 4000.00, 'SALE'),
    ('스낵팩', 60, 3000.00, 'SALE'),
    ('휴지', 70, 2500.00, 'SALE'),
    ('아이스팩', 90, 1500.00, 'SALE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Reservations (샘플)
INSERT INTO reservations (customer_name, start_date, end_date, campsite_id, phone_number, status, reservation_date, confirmation_code) VALUES
    ('홍길동', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, '010-1111-2222', 'CONFIRMED', CURDATE(), 'ABC123'),
    ('김철수', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, '010-3333-4444', 'CONFIRMED', CURDATE(), 'XYZ789')
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name);
