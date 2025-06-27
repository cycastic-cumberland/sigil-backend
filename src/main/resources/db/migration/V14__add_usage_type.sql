ALTER TABLE users
    ADD usage_type SMALLINT NULL;

UPDATE users SET usage_type = 1;

ALTER TABLE users
    MODIFY usage_type SMALLINT NOT NULL;

ALTER TABLE users
    DROP COLUMN lacp_limit;

ALTER TABLE users
    DROP COLUMN project_limit;