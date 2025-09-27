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
@Table(name = "sprints")
@EqualsAndHashCode(callSuper = true)
public class Sprint extends VersionedMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String sprintName;
}
