package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import net.cycastic.sigil.domain.model.tenant.User;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_status_default_assignees")
@EqualsAndHashCode(callSuper = true)
public class TaskStatusDefaultAssignee extends VersionedMetadataEntity {
    @Id
    private Long id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private TaskStatus taskStatus;

    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;
}
