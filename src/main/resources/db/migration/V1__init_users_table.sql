CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       token VARCHAR(500),
                       phone_number VARCHAR(15),
                       address TEXT,
                       created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_date TIMESTAMP
);
