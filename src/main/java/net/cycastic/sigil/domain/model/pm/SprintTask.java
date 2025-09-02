package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.*;

// TODO: Currently unusable, finish the MVP first
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sprint_tasks", indexes = {
        @Index(name = "sprint_tasks_sprint_id_task_id_uindex", columnList = "sprint_id,task_id", unique = true),
        @Index(name = "sprint_tasks_task_id_index", columnList = "task_id"),
})
public class SprintTask {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
