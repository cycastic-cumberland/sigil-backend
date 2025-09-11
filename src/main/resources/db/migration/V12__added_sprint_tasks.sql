CREATE TABLE sprint_tasks
(
    id        INT NOT NULL,
    sprint_id INT NOT NULL,
    task_id   INT NOT NULL,
    CONSTRAINT pk_sprint_tasks PRIMARY KEY (id)
);

ALTER TABLE tasks
    ADD task_identifier VARCHAR(32) NULL;

ALTER TABLE tasks
    ADD tenant_id INT NULL;

ALTER TABLE tasks
    MODIFY task_identifier VARCHAR(32) NOT NULL;

ALTER TABLE tasks
    MODIFY tenant_id INT NOT NULL;

CREATE UNIQUE INDEX sprint_tasks_sprint_id_task_id_uindex ON sprint_tasks (sprint_id, task_id);

CREATE UNIQUE INDEX tasks_tenant_id_task_identifier_uindex ON tasks (tenant_id, task_identifier);

ALTER TABLE sprint_tasks
    ADD CONSTRAINT FK_SPRINT_TASKS_ON_SPRINT FOREIGN KEY (sprint_id) REFERENCES sprints (id);

ALTER TABLE sprint_tasks
    ADD CONSTRAINT FK_SPRINT_TASKS_ON_TASK FOREIGN KEY (task_id) REFERENCES tasks (id);

CREATE INDEX sprint_tasks_task_id_index ON sprint_tasks (task_id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_TENANT FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE tasks
    MODIFY kanban_board_id INT NULL;

ALTER TABLE tasks
    MODIFY task_status_id BIGINT NULL;

ALTER TABLE task_comments
    DROP FOREIGN KEY FK_TASK_COMMENTS_ON_CIPHER;

ALTER TABLE task_comments
    DROP COLUMN cipher_id;