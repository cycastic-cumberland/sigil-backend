ALTER TABLE task_progresses
    ADD version BIGINT NULL;

UPDATE task_progresses SET version = 0;

ALTER TABLE task_progresses
    MODIFY version BIGINT NOT NULL;
