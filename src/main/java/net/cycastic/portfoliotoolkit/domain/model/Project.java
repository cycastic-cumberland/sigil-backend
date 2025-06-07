package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String projectName;

    @Column(nullable = true)
    private String corsSettings;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @OneToMany(mappedBy = "project")
    private Set<Listing> listings;

    @OneToMany(mappedBy = "project")
    private Set<ListingAccessControlPolicy> listingAccessControlPolicies;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;
}
