ALTER TABLE tenant_users
    ADD last_invited datetime NULL;

ALTER TABLE tenant_users
    ADD version BIGINT NULL;

UPDATE tenant_users SET version = 0;

ALTER TABLE tenant_users
    MODIFY version BIGINT NOT NULL;