package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.sigil.domain.model.listing.Listing;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
@Where(clause = "removed_at IS NULL")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String accessControlList;

    @Column(nullable = false)
    private UsageType usageType;

    @Getter
    private long accumulatedAttachmentStorageUsage;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "project")
    private Set<Listing> listings;

    @OneToMany(mappedBy = "project")
    private Set<TenantUser> tenantUsers;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    @Version
    private long version;

    public void setAccumulatedAttachmentStorageUsage(long accumulatedAttachmentStorageUsage){
        this.accumulatedAttachmentStorageUsage = accumulatedAttachmentStorageUsage < 0 ? 0 : accumulatedAttachmentStorageUsage;
    }
}
