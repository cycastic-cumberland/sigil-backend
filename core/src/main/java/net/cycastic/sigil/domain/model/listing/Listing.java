package net.cycastic.sigil.domain.model.listing;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;

import java.time.OffsetDateTime;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "listings", indexes = { @Index(name = "listings_partition_id_listing_path_uindex", columnList = "partition_id,listing_path", unique = true) })
public class Listing extends VersionedMetadataEntity {
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

    private OffsetDateTime removedAt;
}
