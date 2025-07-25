-- Liquibase migration: Initial setup

-- Создание таблицы пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Создание таблицы карт с внешним ключом на пользователя
CREATE TABLE card (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(500) NOT NULL UNIQUE, -- Увеличенный размер для зашифрованных данных
    owner VARCHAR(255) NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Добавление индексов для производительности
CREATE INDEX idx_card_user_id ON card(user_id);
CREATE INDEX idx_card_status ON card(status);
CREATE INDEX idx_users_username ON users(username);

-- Вставка тестовых данных
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$DowJonesIndexAdmin123456789012345678', 'ADMIN'),
('user1', '$2a$10$DowJonesIndexUser1234567890123456789', 'USER');

-- Примечание: пароли должны быть захешированы с помощью BCrypt
-- admin:admin123, user1:user123
