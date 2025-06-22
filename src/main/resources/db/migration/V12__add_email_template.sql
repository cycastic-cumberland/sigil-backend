CREATE TABLE email_templates
(
    id               INT          NOT NULL,
    parameter_string VARCHAR(255) NULL,
    CONSTRAINT pk_email_templates PRIMARY KEY (id)
);

ALTER TABLE email_templates
    ADD CONSTRAINT FK_EMAIL_TEMPLATES_ON_ID FOREIGN KEY (id) REFERENCES attachment_listings (id);