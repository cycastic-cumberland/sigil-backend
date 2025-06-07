ALTER TABLE users
    ADD security_stamp BINARY(32) NULL;

UPDATE users SET security_stamp = RANDOM_BYTES(32);

ALTER TABLE users
    MODIFY security_stamp BINARY(32) NOT NULL;