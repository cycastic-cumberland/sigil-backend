ALTER TABLE attachment_listings
    DROP COLUMN share_token;

ALTER TABLE attachment_listings
    ADD share_token BINARY(16) NULL;

UPDATE attachment_listings SET share_token = (UNHEX(REPLACE(UUID(), '-', '')));

ALTER TABLE attachment_listings
    MODIFY share_token BINARY(16) NOT NULL;