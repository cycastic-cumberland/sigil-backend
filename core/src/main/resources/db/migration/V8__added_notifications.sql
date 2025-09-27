CREATE TABLE notification_settings
(
    id                    INT         NOT NULL,
    user_id               INT         NOT NULL,
    notification_type     VARCHAR(64) NOT NULL,
    notification_disabled BIT(1)      NOT NULL,
    CONSTRAINT pk_notification_settings PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    user_id              INT                   NOT NULL,
    is_read              BIT(1)                NOT NULL,
    notification_content VARCHAR(2000)         NOT NULL,
    notification_type    VARCHAR(64)           NOT NULL,
    created_at           datetime              NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE UNIQUE INDEX notification_settings_user_id_notification_type ON notification_settings (user_id, notification_type);

CREATE INDEX notifications_user_id_created_at_index ON notifications (user_id, created_at);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE notification_settings
    ADD CONSTRAINT FK_NOTIFICATION_SETTINGS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);