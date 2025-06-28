ALTER TABLE attachment_listings
    ADD share_token INT NULL;

UPDATE attachment_listings SET share_token = 0;

ALTER TABLE attachment_listings
    ADD version BIGINT NULL;

UPDATE attachment_listings SET version = 0;

ALTER TABLE attachment_listings
    MODIFY share_token INT NOT NULL;

ALTER TABLE attachment_listings
    MODIFY version BIGINT NOT NULL;