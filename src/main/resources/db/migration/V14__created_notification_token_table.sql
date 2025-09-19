CREATE TABLE notification_tokens
(
    id       BINARY(16)   NOT NULL,
    consumer VARCHAR(255) NOT NULL,
    CONSTRAINT pk_notification_tokens PRIMARY KEY (id)
);

INSERT INTO notification_tokens (id, consumer) SELECT users.notification_token, 'USER' FROM users;

DROP INDEX users_notification_token_uindex ON users;

ALTER TABLE users RENAME COLUMN notification_token TO notification_token_id;

ALTER TABLE users
    ADD CONSTRAINT uc_users_notification_token UNIQUE (notification_token_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_NOTIFICATION_TOKEN FOREIGN KEY (notification_token_id) REFERENCES notification_tokens (id);
