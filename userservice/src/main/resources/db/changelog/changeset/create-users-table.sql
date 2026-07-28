CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    surname VARCHAR(128) NOT NULL,
    birth_date DATE NOT NULL,
    email VARCHAR(128) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
)