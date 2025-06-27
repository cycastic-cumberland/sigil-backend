ALTER TABLE users
    ADD version BIGINT NULL;

UPDATE users SET version = 0;