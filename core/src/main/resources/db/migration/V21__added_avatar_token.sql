ALTER TABLE users
    ADD avatar_token BINARY(16) NULL;

UPDATE users SET users.avatar_token = users.notification_token_id;

ALTER TABLE users
    MODIFY avatar_token BINARY(16) NOT NULL;

CREATE UNIQUE INDEX users_avatar_token_uindex ON users (avatar_token);
