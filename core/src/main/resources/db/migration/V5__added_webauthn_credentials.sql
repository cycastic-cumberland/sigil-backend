CREATE TABLE webauthn_credentials
(
    id                  INT           NOT NULL,
    credential_id       VARBINARY(64) NOT NULL,
    salt                VARBINARY(64) NOT NULL,
    transports          VARCHAR(255)  NOT NULL,
    wrapped_user_key_id BIGINT        NOT NULL,
    CONSTRAINT pk_webauthn_credentials PRIMARY KEY (id)
);

ALTER TABLE webauthn_credentials
    ADD CONSTRAINT uc_webauthn_credentials_wrapped_user_key UNIQUE (wrapped_user_key_id);

ALTER TABLE webauthn_credentials
    ADD CONSTRAINT FK_WEBAUTHN_CREDENTIALS_ON_ID FOREIGN KEY (id) REFERENCES users (id);

ALTER TABLE webauthn_credentials
    ADD CONSTRAINT FK_WEBAUTHN_CREDENTIALS_ON_WRAPPED_USER_KEY FOREIGN KEY (wrapped_user_key_id) REFERENCES cipher_store (id);

DROP INDEX cipher_store_kid_uindex
    ON cipher_store;

ALTER TABLE cipher_store
    DROP COLUMN kid;