CREATE TABLE product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  description TEXT,
  isin VARCHAR(255),
  min_price VARCHAR(255),
  max_price VARCHAR(255),
  rating VARCHAR(255)
);
commit;

CREATE TABLE review (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  date VARCHAR(255),
  text TEXT,
  rating TEXT,
  product_id BIGINT NOT NULL,
  FOREIGN KEY (product_id) REFERENCES product(id)
);
commit;
