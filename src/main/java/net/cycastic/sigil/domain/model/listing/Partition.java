package net.cycastic.sigil.domain.model.listing;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partitions", indexes = @Index(name = "partitions_tenant_id_partition_path_uindex", columnList = "tenant_id,partition_path", unique = true))
@Where(clause = "removed_at IS NULL")
public class Partition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tenant_id", nullable=false)
    private Tenant tenant;

    @Column(columnDefinition = "VARCHAR(512)", nullable = false)
    private String partitionPath;

    @OneToOne
    @JoinColumn(name = "server_partition_key_id")
    private Cipher serverPartitionKey;

    @Column(name = "md5Digest", columnDefinition = "BINARY(16)", nullable = false)
    private byte[] keyMd5Digest;

    @Column(name = "sha256Digest", columnDefinition = "BINARY(32)", nullable = false)
    private byte[] keySha256Digest;

    @OneToMany(mappedBy = "partition")
    private Set<Listing> listings;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    @Version
    private long version;
}
