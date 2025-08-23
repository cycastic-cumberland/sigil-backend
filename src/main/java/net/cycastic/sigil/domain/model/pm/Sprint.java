package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sprints")
@EqualsAndHashCode(callSuper = true)
public class Sprint extends VersionedMetadataEntity {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String sprintName;
}
