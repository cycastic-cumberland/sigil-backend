CREATE TABLE listing_access_control_policies
(
    id              INT AUTO_INCREMENT NOT NULL,
    low_search_key  VARBINARY(255)     NULL,
    high_search_key VARBINARY(255)     NULL,
    priority        INT                NOT NULL,
    is_allowed      BIT(1)             NOT NULL,
    project_id      INT                NOT NULL,
    apply_to_id     INT                NULL,
    CONSTRAINT pk_listing_access_control_policies PRIMARY KEY (id)
);

ALTER TABLE projects
    ADD cors_settings VARCHAR(255) NULL;

ALTER TABLE listing_access_control_policies
    ADD CONSTRAINT FK_LISTING_ACCESS_CONTROL_POLICIES_ON_APPLY_TO FOREIGN KEY (apply_to_id) REFERENCES users (id);

ALTER TABLE listing_access_control_policies
    ADD CONSTRAINT FK_LISTING_ACCESS_CONTROL_POLICIES_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);