ALTER TABLE attachment_listings
    DROP COLUMN encryption_key_id;

ALTER TABLE users
    DROP COLUMN hashed_password;