-- 테이블 구조만 정의 (데이터 없음)

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  stock_quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  product_type VARCHAR(32) NOT NULL,
  UNIQUE KEY uk_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campsites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  site_number VARCHAR(32) NOT NULL,
  description VARCHAR(255),
  max_people INT NOT NULL,
  UNIQUE KEY uk_campsites_site_number (site_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  reservation_date DATE NOT NULL,
  campsite_id BIGINT NOT NULL,
  phone_number VARCHAR(32),
  status VARCHAR(32) NOT NULL,
  confirmation_code VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_reservations_confirmation_code (confirmation_code),
  CONSTRAINT fk_reservation_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sales_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sales_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rental_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  is_returned TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rental_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
  CONSTRAINT fk_rental_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;