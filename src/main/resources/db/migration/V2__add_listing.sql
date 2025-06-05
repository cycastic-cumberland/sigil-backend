CREATE TABLE listings
(
    id             INT AUTO_INCREMENT NOT NULL,
    project_id     INT                NOT NULL,
    search_key     VARBINARY(255)     NOT NULL,
    standard_value VARCHAR(255)       NULL,
    long_value     TEXT               NULL,
    created_at     datetime           NOT NULL,
    updated_at     datetime           NULL,
    removed_at     datetime           NULL,
    CONSTRAINT pk_listings PRIMARY KEY (id)
);

CREATE INDEX listings_searchKey_index ON listings (search_key);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);