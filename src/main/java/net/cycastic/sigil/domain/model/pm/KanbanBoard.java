package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kanban_boards")
@EqualsAndHashCode(callSuper = true)
public class KanbanBoard extends VersionedMetadataEntity {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_partition_id", nullable = false)
    private ProjectPartition projectPartition;

    @Column(nullable = false)
    private String boardName;
}
