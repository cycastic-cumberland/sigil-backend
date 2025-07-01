package net.cycastic.portfoliotoolkit.domain.model.listing;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.model.Project;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "listings", indexes = { @Index(name = "listings_project_id_listing_path_uindex", columnList = "project_id,listing_path", unique = true) })
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @NotNull
    @Column(columnDefinition = "VARCHAR(512)")
    private String listingPath;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private ListingType type;

    @OneToOne(mappedBy = "listing")
    private TextListing textListing;

    @OneToOne(mappedBy = "listing")
    private DecimalListing decimalListing;

    @OneToOne(mappedBy = "listing")
    private AttachmentListing attachmentListing;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    @Version
    private long version;
}
