CREATE TABLE attachment_listings
(
    id                   INT          NOT NULL,
    bucket_name          VARCHAR(32)  NULL,
    bucket_region        VARCHAR(16)  NULL,
    object_key           VARCHAR(255) NOT NULL,
    mime_type            VARCHAR(255) NULL,
    upload_completed     BIT(1)       NOT NULL,
    encryption_detail_id BIGINT       NULL,
    version              BIGINT       NOT NULL,
    CONSTRAINT pk_attachment_listings PRIMARY KEY (id)
);

CREATE TABLE cipher_store
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    kid               BINARY(32)            NULL,
    decryption_method SMALLINT              NOT NULL,
    iv                BINARY(12)            NULL,
    cipher_standard   VARBINARY(512)        NULL,
    cipher_long       MEDIUMBLOB            NULL,
    CONSTRAINT pk_cipher_store PRIMARY KEY (id)
);

CREATE TABLE listings
(
    id           INT AUTO_INCREMENT NOT NULL,
    tenant_id    INT                NOT NULL,
    listing_path VARCHAR(512)       NOT NULL,
    type         SMALLINT           NOT NULL,
    created_at   datetime           NOT NULL,
    updated_at   datetime           NULL,
    removed_at   datetime           NULL,
    version      BIGINT             NOT NULL,
    CONSTRAINT pk_listings PRIMARY KEY (id)
);

CREATE TABLE tenant_users
(
    id                    INT AUTO_INCREMENT NOT NULL,
    tenant_id             INT                NOT NULL,
    user_id               INT                NOT NULL,
    wrapped_tenant_key_id BIGINT             NOT NULL,
    CONSTRAINT pk_tenant_users PRIMARY KEY (id)
);

CREATE TABLE tenants
(
    id                                   INT AUTO_INCREMENT NOT NULL,
    name                                 VARCHAR(255)       NOT NULL,
    access_control_list                  VARCHAR(255)       NULL,
    usage_type                           SMALLINT           NOT NULL,
    accumulated_attachment_storage_usage BIGINT             NOT NULL,
    owner_id                             INT                NULL,
    created_at                           datetime           NOT NULL,
    updated_at                           datetime           NULL,
    removed_at                           datetime           NULL,
    version                              BIGINT             NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                   INT AUTO_INCREMENT NOT NULL,
    email                VARCHAR(255)       NOT NULL,
    normalized_email     VARCHAR(255)       NOT NULL,
    first_name           VARCHAR(255)       NULL,
    last_name            VARCHAR(255)       NULL,
    hashed_password      VARCHAR(255)       NULL,
    roles                VARCHAR(255)       NOT NULL,
    joined_at            datetime           NOT NULL,
    security_stamp       BINARY(32)         NOT NULL,
    version              BIGINT             NOT NULL,
    status               SMALLINT           NOT NULL,
    last_invitation_sent datetime           NULL,
    email_verified       BIT(1)             NOT NULL,
    public_rsa_key       VARBINARY(512)     NULL,
    wrapped_user_key_id  BIGINT             NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE attachment_listings
    ADD CONSTRAINT uc_attachment_listings_encryption_detail UNIQUE (encryption_detail_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_wrapped_user_key UNIQUE (wrapped_user_key_id);

CREATE INDEX cipher_store_kid_uindex ON cipher_store (kid);

CREATE UNIQUE INDEX listings_tenant_id_listing_path_uindex ON listings (tenant_id, listing_path);

CREATE UNIQUE INDEX tenant_users_tenant_id_user_id_uindex ON tenant_users (tenant_id, user_id);

CREATE UNIQUE INDEX users_normalized_email_uindex ON users (normalized_email);

ALTER TABLE attachment_listings
    ADD CONSTRAINT FK_ATTACHMENT_LISTINGS_ON_ENCRYPTION_DETAIL FOREIGN KEY (encryption_detail_id) REFERENCES cipher_store (id);

ALTER TABLE attachment_listings
    ADD CONSTRAINT FK_ATTACHMENT_LISTINGS_ON_ID FOREIGN KEY (id) REFERENCES listings (id);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_TENANT FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE tenants
    ADD CONSTRAINT FK_TENANTS_ON_OWNER FOREIGN KEY (owner_id) REFERENCES users (id);

ALTER TABLE tenant_users
    ADD CONSTRAINT FK_TENANT_USERS_ON_TENANT FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE tenant_users
    ADD CONSTRAINT FK_TENANT_USERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE tenant_users
    ADD CONSTRAINT FK_TENANT_USERS_ON_WRAPPED_TENANT_KEY FOREIGN KEY (wrapped_tenant_key_id) REFERENCES cipher_store (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_WRAPPED_USER_KEY FOREIGN KEY (wrapped_user_key_id) REFERENCES cipher_store (id);