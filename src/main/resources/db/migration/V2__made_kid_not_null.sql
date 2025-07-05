ALTER TABLE attachment_listings
    DROP FOREIGN KEY FK_ATTACHMENT_LISTINGS_ON_ENCRYPTION_DETAIL;

ALTER TABLE attachment_listings
    ADD content_length BIGINT NULL;

ALTER TABLE attachment_listings
    ADD encryption_key_id BINARY(32) NULL;

ALTER TABLE attachment_listings
    MODIFY content_length BIGINT NOT NULL;

ALTER TABLE attachment_listings
    DROP COLUMN encryption_detail_id;

ALTER TABLE cipher_store
    MODIFY kid BINARY(32) NOT NULL;