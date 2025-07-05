package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "listing_access_control_policies", indexes = { @Index(name = "listing_access_control_policies_priority_project_id_uindex", columnList = "project_id,priority", unique = true) })
public class ListingAccessControlPolicy {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARBINARY(255)")
    private String globPath;

    private int priority;

    private boolean isAllowed;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(nullable = true)
    private Integer applyToId; // `null` for everyone
}
