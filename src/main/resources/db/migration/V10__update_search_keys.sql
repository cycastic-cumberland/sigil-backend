CREATE TABLE encrypted_smtp_credentials
(
    id             INT AUTO_INCREMENT NOT NULL,
    project_id     INT                NOT NULL,
    server_address VARCHAR(255)       NOT NULL,
    secure_smtp    VARCHAR(255)       NOT NULL,
    port           INT                NOT NULL,
    timeout        INT                NOT NULL,
    from_address   VARCHAR(255)       NOT NULL,
    password       VARCHAR(255)       NOT NULL,
    from_name      VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_encrypted_smtp_credentials PRIMARY KEY (id)
);

ALTER TABLE listing_access_control_policies
    ADD glob_path VARBINARY(255) NULL;

ALTER TABLE listings
    ADD listing_path VARBINARY(255) NULL;

ALTER TABLE listings
    MODIFY listing_path VARBINARY(255) NOT NULL;

ALTER TABLE attachment_listings
    ADD upload_completed BIT(1) NULL;

ALTER TABLE attachment_listings
    MODIFY upload_completed BIT(1) NOT NULL;

CREATE UNIQUE INDEX listings_project_id_listing_path_uindex ON listings (project_id, listing_path);

ALTER TABLE encrypted_smtp_credentials
    ADD CONSTRAINT FK_ENCRYPTED_SMTP_CREDENTIALS_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE listing_access_control_policies
    DROP COLUMN high_search_key;

ALTER TABLE listing_access_control_policies
    DROP COLUMN low_search_key;

ALTER TABLE listings DROP INDEX listings_project_id_searchKey_uindex;

ALTER TABLE listings
    DROP COLUMN search_key;

