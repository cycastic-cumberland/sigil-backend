CREATE TABLE projects
(
    id           INT AUTO_INCREMENT NOT NULL,
    project_name VARCHAR(255)       NOT NULL,
    user_id      INT                NOT NULL,
    created_at   datetime           NOT NULL,
    updated_at   datetime           NULL,
    removed_at   datetime           NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id)
);

CREATE TABLE users
(
    id               INT AUTO_INCREMENT NOT NULL,
    email            VARCHAR(255)       NOT NULL,
    normalized_email VARCHAR(255)       NOT NULL,
    first_name       VARCHAR(255)       NOT NULL,
    last_name        VARCHAR(255)       NOT NULL,
    hashed_password  VARCHAR(255)       NOT NULL,
    roles            VARCHAR(255)       NOT NULL,
    disabled         BIT(1)             NOT NULL,
    joined_at        datetime           NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE UNIQUE INDEX users_normalized_email_uindex ON users (normalized_email);

ALTER TABLE projects
    ADD CONSTRAINT FK_PROJECTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);