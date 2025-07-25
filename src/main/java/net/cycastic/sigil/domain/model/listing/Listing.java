package net.cycastic.sigil.domain.model.listing;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "listings", indexes = { @Index(name = "listings_partition_id_listing_path_uindex", columnList = "partition_id,listing_path", unique = true) })
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="partition_id", nullable=false)
    private Partition partition;

    @NotNull
    @Column(columnDefinition = "VARCHAR(512)")
    private String listingPath;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private ListingType type;

    @OneToOne(mappedBy = "listing")
    private AttachmentListing attachmentListing;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    @Version
    private long version;
}
