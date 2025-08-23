package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_progresses", indexes = {
        @Index(name = "task_progresses_uindex", columnList = "from_status_id,next_status_id", unique = true)
})
public class TaskProgress {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "from_status_id", nullable = false)
    private TaskStatus fromStatus;

    @ManyToOne
    @JoinColumn(name = "next_status_id", nullable = false)
    private TaskStatus nextStatus;
}
