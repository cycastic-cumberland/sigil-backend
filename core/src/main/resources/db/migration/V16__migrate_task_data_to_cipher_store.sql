ALTER TABLE tasks
    ADD encrypted_content_cipher_id BIGINT NULL;

ALTER TABLE tasks
    ADD encrypted_name_cipher_id BIGINT NULL;

ALTER TABLE tasks
    MODIFY encrypted_name_cipher_id BIGINT NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT uc_tasks_encrypted_content_cipher UNIQUE (encrypted_content_cipher_id);

ALTER TABLE tasks
    ADD CONSTRAINT uc_tasks_encrypted_name_cipher UNIQUE (encrypted_name_cipher_id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_ENCRYPTED_CONTENT_CIPHER FOREIGN KEY (encrypted_content_cipher_id) REFERENCES cipher_store (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_ENCRYPTED_NAME_CIPHER FOREIGN KEY (encrypted_name_cipher_id) REFERENCES cipher_store (id);

ALTER TABLE tasks
    DROP COLUMN encrypted_content;

ALTER TABLE tasks
    DROP COLUMN encrypted_name;

ALTER TABLE tasks
    DROP COLUMN iv;

ALTER TABLE cipher_store
    ADD version BIGINT NULL;

UPDATE cipher_store SET version = 0;

ALTER TABLE cipher_store
    MODIFY version BIGINT NOT NULL;