package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.tenant.Tenant;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_partitions", indexes = {
        @Index(name = "project_partitions_tenant_id_unique_identifier_uindex", columnList = "tenant_id,unique_identifier", unique = true)
})
@EqualsAndHashCode(callSuper = true)
public class ProjectPartition extends VersionedMetadataEntity {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Partition partition;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(columnDefinition = "VARCHAR(16)", nullable = false)
    private String uniqueIdentifier;

    private int latestSprintNumber;

    private int latestTaskId;
}
