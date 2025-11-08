ALTER TABLE task_progresses
    ADD progression_name VARCHAR(255) NULL;

UPDATE task_progresses SET progression_name = 'to next status';

ALTER TABLE task_progresses
    MODIFY progression_name VARCHAR(255) NOT NULL;

ALTER TABLE task_progresses
    MODIFY id BIGINT AUTO_INCREMENT;