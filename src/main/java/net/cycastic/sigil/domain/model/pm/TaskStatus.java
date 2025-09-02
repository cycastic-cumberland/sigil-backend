package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import jakarta.annotation.Nullable;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_statuses")
@EqualsAndHashCode(callSuper = true)
public class TaskStatus extends VersionedMetadataEntity {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kanban_board_id", nullable = false)
    private KanbanBoard kanbanBoard;

    @Column(nullable = false)
    private String statusName;

    @Nullable
    @OneToOne(mappedBy = "taskStatus")
    private TaskUniqueStatus taskUniqueStatus;

    @Nullable
    @OneToOne(mappedBy = "taskStatus")
    private TaskStatusDefaultAssignee taskStatusDefaultAssignee;
}
