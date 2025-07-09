package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.listing.Listing;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partitions", indexes = @Index(name = "partitions_tenant_id_partition_path_uindex", columnList = "tenant_id,partition_path", unique = true))
public class Partition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tenant_id", nullable=false)
    private Tenant tenant;

    private String partitionPath;

    @OneToOne
    @JoinColumn(name = "server_partition_key_id")
    private Cipher serverPartitionKey;

    @OneToMany(mappedBy = "partition")
    private Set<Listing> listings;
}
