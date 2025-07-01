ALTER TABLE users
    ADD email_verified BIT(1) NULL;

UPDATE users SET email_verified = true;

ALTER TABLE users
    MODIFY email_verified BIT(1) NOT NULL;