ALTER TABLE users
    ADD accumulated_attachment_storage_usage BIGINT NULL;

UPDATE users SET accumulated_attachment_storage_usage = 0;

ALTER TABLE users
    MODIFY accumulated_attachment_storage_usage BIGINT NOT NULL;