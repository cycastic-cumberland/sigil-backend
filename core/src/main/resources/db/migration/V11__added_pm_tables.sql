CREATE TABLE kanban_boards
(
    id                   INT          NOT NULL,
    version              BIGINT       NOT NULL,
    created_at           datetime     NOT NULL,
    updated_at           datetime     NULL,
    project_partition_id INT          NOT NULL,
    board_name           VARCHAR(255) NOT NULL,
    CONSTRAINT pk_kanban_boards PRIMARY KEY (id)
);

CREATE TABLE project_partitions
(
    id                   INT         NOT NULL,
    version              BIGINT      NOT NULL,
    created_at           datetime    NOT NULL,
    updated_at           datetime    NULL,
    tenant_id            INT         NOT NULL,
    unique_identifier    VARCHAR(16) NOT NULL,
    latest_sprint_number INT         NOT NULL,
    latest_task_id       INT         NOT NULL,
    CONSTRAINT pk_project_partitions PRIMARY KEY (id)
);

CREATE TABLE sprints
(
    id          INT          NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NULL,
    sprint_name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_sprints PRIMARY KEY (id)
);

CREATE TABLE task_comments
(
    id                BIGINT     NOT NULL,
    version           BIGINT     NOT NULL,
    created_at        datetime   NOT NULL,
    updated_at        datetime   NULL,
    task_id           INT        NOT NULL,
    sender_id         INT        NOT NULL,
    encrypted_content BLOB       NOT NULL,
    iv                BINARY(12) NOT NULL,
    cipher_id         BIGINT     NOT NULL,
    CONSTRAINT pk_task_comments PRIMARY KEY (id)
);

CREATE TABLE task_progresses
(
    id             INT    NOT NULL,
    from_status_id BIGINT NOT NULL,
    next_status_id BIGINT NOT NULL,
    CONSTRAINT pk_task_progresses PRIMARY KEY (id)
);

CREATE TABLE task_status_default_assignees
(
    id          BIGINT   NOT NULL,
    version     BIGINT   NOT NULL,
    created_at  datetime NOT NULL,
    updated_at  datetime NULL,
    assignee_id INT      NOT NULL,
    CONSTRAINT pk_task_status_default_assignees PRIMARY KEY (id)
);

CREATE TABLE task_statuses
(
    id              BIGINT       NOT NULL,
    version         BIGINT       NOT NULL,
    created_at      datetime     NOT NULL,
    updated_at      datetime     NULL,
    kanban_board_id INT          NOT NULL,
    status_name     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_task_statuses PRIMARY KEY (id)
);

CREATE TABLE task_subscribers
(
    id            BIGINT NOT NULL,
    task_id       INT    NOT NULL,
    subscriber_id INT    NOT NULL,
    CONSTRAINT pk_task_subscribers PRIMARY KEY (id)
);

CREATE TABLE task_unique_statuses
(
    id                     BIGINT   NOT NULL,
    kanban_board_id        INT      NOT NULL,
    task_unique_stereotype SMALLINT NOT NULL,
    version                BIGINT   NOT NULL,
    CONSTRAINT pk_task_unique_statuses PRIMARY KEY (id)
);

CREATE TABLE tasks
(
    id                INT          NOT NULL,
    version           BIGINT       NOT NULL,
    created_at        datetime     NOT NULL,
    updated_at        datetime     NULL,
    kanban_board_id   INT          NOT NULL,
    task_status_id    BIGINT       NOT NULL,
    assignee_id       INT          NULL,
    reporter_id       INT          NULL,
    priority          SMALLINT     NOT NULL,
    encrypted_name    BLOB         NOT NULL,
    encrypted_content BLOB         NULL,
    iv                BINARY(12)   NOT NULL,
    label             VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);

ALTER TABLE partitions
    ADD partition_type SMALLINT NULL;

UPDATE partitions SET partition_type = 0;

ALTER TABLE partitions
    MODIFY partition_type SMALLINT NOT NULL;

ALTER TABLE task_unique_statuses
    ADD CONSTRAINT uc_task_unique_statuses_kanban_board UNIQUE (kanban_board_id);

CREATE UNIQUE INDEX project_partitions_tenant_id_unique_identifier_uindex ON project_partitions (tenant_id, unique_identifier);

CREATE INDEX task_comments_task_id_created_at_index ON task_comments (task_id, created_at);

CREATE UNIQUE INDEX task_progresses_uindex ON task_progresses (from_status_id, next_status_id);

CREATE UNIQUE INDEX task_subscribers_task_id_subscriber_id_uindex ON task_subscribers (task_id, subscriber_id);

CREATE UNIQUE INDEX task_unique_statuses_uindex ON task_unique_statuses (kanban_board_id, task_unique_stereotype);

CREATE INDEX tasks_kanban_board_id_assignee_id_index ON tasks (kanban_board_id, assignee_id);

CREATE INDEX tasks_kanban_board_id_priority_index ON tasks (kanban_board_id, priority);

CREATE INDEX tasks_kanban_board_id_task_status_id_index ON tasks (kanban_board_id, task_status_id);

ALTER TABLE kanban_boards
    ADD CONSTRAINT FK_KANBAN_BOARDS_ON_PROJECT_PARTITION FOREIGN KEY (project_partition_id) REFERENCES project_partitions (id);

ALTER TABLE project_partitions
    ADD CONSTRAINT FK_PROJECT_PARTITIONS_ON_ID FOREIGN KEY (id) REFERENCES partitions (id);

ALTER TABLE project_partitions
    ADD CONSTRAINT FK_PROJECT_PARTITIONS_ON_TENANT FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_ASSIGNEE FOREIGN KEY (assignee_id) REFERENCES users (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_KANBAN_BOARD FOREIGN KEY (kanban_board_id) REFERENCES kanban_boards (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_REPORTER FOREIGN KEY (reporter_id) REFERENCES users (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_TASK_STATUS FOREIGN KEY (task_status_id) REFERENCES task_statuses (id);

ALTER TABLE task_comments
    ADD CONSTRAINT FK_TASK_COMMENTS_ON_CIPHER FOREIGN KEY (cipher_id) REFERENCES cipher_store (id);

ALTER TABLE task_comments
    ADD CONSTRAINT FK_TASK_COMMENTS_ON_SENDER FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE task_comments
    ADD CONSTRAINT FK_TASK_COMMENTS_ON_TASK FOREIGN KEY (task_id) REFERENCES tasks (id);

ALTER TABLE task_progresses
    ADD CONSTRAINT FK_TASK_PROGRESSES_ON_FROM_STATUS FOREIGN KEY (from_status_id) REFERENCES task_statuses (id);

ALTER TABLE task_progresses
    ADD CONSTRAINT FK_TASK_PROGRESSES_ON_NEXT_STATUS FOREIGN KEY (next_status_id) REFERENCES task_statuses (id);

ALTER TABLE task_statuses
    ADD CONSTRAINT FK_TASK_STATUSES_ON_KANBAN_BOARD FOREIGN KEY (kanban_board_id) REFERENCES kanban_boards (id);

ALTER TABLE task_status_default_assignees
    ADD CONSTRAINT FK_TASK_STATUS_DEFAULT_ASSIGNEES_ON_ASSIGNEE FOREIGN KEY (assignee_id) REFERENCES users (id);

ALTER TABLE task_status_default_assignees
    ADD CONSTRAINT FK_TASK_STATUS_DEFAULT_ASSIGNEES_ON_ID FOREIGN KEY (id) REFERENCES task_statuses (id);

ALTER TABLE task_subscribers
    ADD CONSTRAINT FK_TASK_SUBSCRIBERS_ON_SUBSCRIBER FOREIGN KEY (subscriber_id) REFERENCES users (id);

ALTER TABLE task_subscribers
    ADD CONSTRAINT FK_TASK_SUBSCRIBERS_ON_TASK FOREIGN KEY (task_id) REFERENCES tasks (id);

ALTER TABLE task_unique_statuses
    ADD CONSTRAINT FK_TASK_UNIQUE_STATUSES_ON_ID FOREIGN KEY (id) REFERENCES task_statuses (id);

ALTER TABLE task_unique_statuses
    ADD CONSTRAINT FK_TASK_UNIQUE_STATUSES_ON_KANBAN_BOARD FOREIGN KEY (kanban_board_id) REFERENCES kanban_boards (id);