package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_unique_statuses", indexes = {
        @Index(name = "task_unique_statuses_uindex", columnList = "kanban_board_id,task_unique_stereotype", unique = true)
})
public class TaskUniqueStatus {
    @Id
    private Long id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private TaskStatus taskStatus;

    @OneToOne
    @JoinColumn(name = "kanban_board_id", nullable = false)
    private KanbanBoard kanbanBoard;

    @Column(nullable = false)
    private TaskUniqueStereotype taskUniqueStereotype;

    @Version
    private long version;
}
