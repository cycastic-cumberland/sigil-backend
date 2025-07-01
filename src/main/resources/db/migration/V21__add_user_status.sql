ALTER TABLE users
    ADD last_invitation_sent datetime NULL;

ALTER TABLE users
    ADD status SMALLINT NULL;

UPDATE users SET status = 1;

ALTER TABLE users
    MODIFY status SMALLINT NOT NULL;