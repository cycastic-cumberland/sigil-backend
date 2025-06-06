package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "listings", indexes = { @Index(name = "listings_searchKey_index", columnList = "search_key") })
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @NotNull
    @Column(columnDefinition = "VARBINARY(255)")
    private byte[] searchKey;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private ListingType type;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;
}
