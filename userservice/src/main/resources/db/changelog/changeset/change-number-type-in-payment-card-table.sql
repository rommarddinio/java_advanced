ALTER TABLE payment_cards
DROP COLUMN number,
ADD COLUMN number VARCHAR(20);