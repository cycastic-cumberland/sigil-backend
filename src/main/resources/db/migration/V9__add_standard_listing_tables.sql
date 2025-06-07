CREATE TABLE attachment_listings
(
    id            INT          NOT NULL,
    bucket_name   VARCHAR(32)  NULL,
    bucket_region VARCHAR(16)  NULL,
    object_key    VARCHAR(255) NOT NULL,
    mime_type     VARCHAR(255) NULL,
    CONSTRAINT pk_attachment_listings PRIMARY KEY (id)
);

CREATE TABLE decimal_listings
(
    id     INT             NOT NULL,
    number DECIMAL(38, 18) NOT NULL,
    CONSTRAINT pk_decimal_listings PRIMARY KEY (id)
);

CREATE TABLE text_listings
(
    id          INT          NOT NULL,
    text_normal VARCHAR(255) NULL,
    text_long   TEXT         NULL,
    CONSTRAINT pk_text_listings PRIMARY KEY (id)
);

ALTER TABLE attachment_listings
    ADD CONSTRAINT FK_ATTACHMENT_LISTINGS_ON_ID FOREIGN KEY (id) REFERENCES listings (id);

ALTER TABLE decimal_listings
    ADD CONSTRAINT FK_DECIMAL_LISTINGS_ON_ID FOREIGN KEY (id) REFERENCES listings (id);

ALTER TABLE text_listings
    ADD CONSTRAINT FK_TEXT_LISTINGS_ON_ID FOREIGN KEY (id) REFERENCES listings (id);