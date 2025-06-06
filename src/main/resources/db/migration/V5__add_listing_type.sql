ALTER TABLE listings
    ADD type SMALLINT NULL;

UPDATE listings SET type = 0;

ALTER TABLE listings
    MODIFY type SMALLINT NOT NULL;

ALTER TABLE listings
    DROP COLUMN long_value;

ALTER TABLE listings
    DROP COLUMN standard_value;