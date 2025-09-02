package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.User;

import java.util.Collection;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks", indexes = {
        @Index(name = "tasks_tenant_id_task_identifier_uindex", columnList = "tenant_id,task_identifier", unique = true),
        @Index(name = "tasks_kanban_board_id_task_status_id_index", columnList = "kanban_board_id,task_status_id"),
        @Index(name = "tasks_kanban_board_id_assignee_id_index", columnList = "kanban_board_id,assignee_id"),
        @Index(name = "tasks_kanban_board_id_priority_index", columnList = "kanban_board_id,priority"),
})
@EqualsAndHashCode(callSuper = true)
public class Task extends VersionedMetadataEntity {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(columnDefinition = "VARCHAR(32)", nullable = false)
    private String taskIdentifier;

    @ManyToOne
    @JoinColumn(name = "kanban_board_id")
    private KanbanBoard kanbanBoard;

    @ManyToOne
    @JoinColumn(name = "task_status_id")
    private TaskStatus taskStatus;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private byte[] encryptedName;

    private byte[] encryptedContent;

    @Column(columnDefinition = "BINARY(12)", nullable = false)
    private byte[] iv;

    @Column(nullable = false)
    private String label;

    @OneToMany(mappedBy = "task")
    private Collection<TaskSubscriber> subscribers;
}
