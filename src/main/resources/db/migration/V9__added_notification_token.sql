ALTER TABLE users
    ADD notification_token BINARY(16) NULL;

DROP FUNCTION IF EXISTS UUID_TO_BIN;

DELIMITER //
CREATE FUNCTION UUID_TO_BIN(uuid CHAR(36)) RETURNS BINARY(16)
BEGIN
    RETURN UNHEX(REPLACE(uuid, '-', ''));
END//
DELIMITER ;

UPDATE users SET notification_token = UUID_TO_BIN(UUID());

ALTER TABLE users
    MODIFY notification_token BINARY(16) NOT NULL;

CREATE UNIQUE INDEX users_notification_token_uindex ON users (notification_token);