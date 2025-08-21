TRUNCATE TABLE notifications;

ALTER TABLE notifications
    ADD encryption_cipher BIGINT NULL;

ALTER TABLE notifications
    MODIFY encryption_cipher BIGINT NOT NULL;

ALTER TABLE notifications
    ADD CONSTRAINT uc_notifications_encryption_cipher UNIQUE (encryption_cipher);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_ENCRYPTION_CIPHER FOREIGN KEY (encryption_cipher) REFERENCES cipher_store (id);

ALTER TABLE notifications
    DROP COLUMN notification_content;

ALTER TABLE notifications
    ADD notification_content VARBINARY(2000) NOT NULL;