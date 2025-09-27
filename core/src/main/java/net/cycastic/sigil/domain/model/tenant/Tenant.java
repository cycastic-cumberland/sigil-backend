package net.cycastic.sigil.domain.model.tenant;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
@Where(clause = "removed_at IS NULL")
@EqualsAndHashCode(callSuper = true)
public class Tenant extends VersionedMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UsageType usageType;

    @Getter
    private long accumulatedAttachmentStorageUsage;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "tenant")
    private Set<TenantUser> tenantUsers;

    private OffsetDateTime removedAt;

    public void setAccumulatedAttachmentStorageUsage(long accumulatedAttachmentStorageUsage){
        this.accumulatedAttachmentStorageUsage = accumulatedAttachmentStorageUsage < 0 ? 0 : accumulatedAttachmentStorageUsage;
    }
}
