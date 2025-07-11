ALTER TABLE partitions
    ADD md5digest BINARY(16) NULL;

ALTER TABLE partitions
    ADD sha256digest BINARY(32) NULL;

ALTER TABLE partitions
    MODIFY md5digest BINARY(16) NOT NULL;

ALTER TABLE partitions
    MODIFY sha256digest BINARY(32) NOT NULL;