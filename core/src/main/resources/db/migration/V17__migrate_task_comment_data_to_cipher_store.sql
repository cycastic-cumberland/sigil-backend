ALTER TABLE task_comments
    ADD encrypted_content_cipher_id BIGINT NULL;

ALTER TABLE task_comments
    ADD CONSTRAINT uc_task_comments_encrypted_content_cipher UNIQUE (encrypted_content_cipher_id);

ALTER TABLE task_comments
    ADD CONSTRAINT FK_TASK_COMMENTS_ON_ENCRYPTED_CONTENT_CIPHER FOREIGN KEY (encrypted_content_cipher_id) REFERENCES cipher_store (id);

ALTER TABLE task_comments
    DROP COLUMN encrypted_content;

ALTER TABLE task_comments
    DROP COLUMN iv;