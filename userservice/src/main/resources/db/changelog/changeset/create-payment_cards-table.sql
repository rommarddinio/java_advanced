CREATE TABLE payment_cards(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    number VARCHAR(16) UNIQUE NOT NULL,
    holder VARCHAR(256) NOT NULL,
    expiration_date DATE NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
)