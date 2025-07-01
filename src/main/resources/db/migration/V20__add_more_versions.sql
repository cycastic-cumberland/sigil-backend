ALTER TABLE encrypted_smtp_credentials
    ADD version BIGINT NULL;

UPDATE encrypted_smtp_credentials SET version = 0;

ALTER TABLE encrypted_smtp_credentials
    MODIFY version BIGINT NOT NULL;

ALTER TABLE listings
    ADD version BIGINT NULL;

UPDATE listings SET version = 0;

ALTER TABLE listings
    MODIFY version BIGINT NOT NULL;

ALTER TABLE projects
    ADD version BIGINT NULL;

UPDATE projects SET version = 0;

ALTER TABLE projects
    MODIFY version BIGINT NOT NULL;