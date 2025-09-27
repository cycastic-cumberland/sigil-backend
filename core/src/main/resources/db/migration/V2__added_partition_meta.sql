ALTER TABLE partitions
    ADD created_at datetime NULL;

UPDATE partitions SET created_at = NOW();

ALTER TABLE partitions
    ADD removed_at datetime NULL;

ALTER TABLE partitions
    ADD updated_at datetime NULL;

ALTER TABLE partitions
    ADD version BIGINT NULL;

UPDATE partitions SET version = 0;

ALTER TABLE partitions
    MODIFY created_at datetime NOT NULL;

ALTER TABLE partitions
    MODIFY version BIGINT NOT NULL;

ALTER TABLE tenants
    DROP COLUMN access_control_list;

ALTER TABLE partitions
    MODIFY partition_path VARCHAR(512) NOT NULL;