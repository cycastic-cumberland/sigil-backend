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
@Table(name = "projects")
@Where(clause = "removed_at IS NULL")
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

    @OneToMany(mappedBy = "project")
    private Set<EncryptedSmtpCredential> encryptedSmtpCredentials;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    @Version
    private long version;
}
