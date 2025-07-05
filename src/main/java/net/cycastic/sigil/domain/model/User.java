package net.cycastic.sigil.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import jakarta.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = { @Index(name = "users_normalized_email_uindex", columnList = "normalized_email", unique = true) })
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String email;

    @NotNull
    private String normalizedEmail;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String hashedPassword;

    @NotNull
    private String roles;

    @Column(nullable = false)
    private UsageType usageType;

    @NotNull
    private OffsetDateTime joinedAt;

    @NotNull
    @Column(columnDefinition = "BINARY(32)")
    private byte[] securityStamp;

    @Version
    private long version;

    @Getter
    private long accumulatedAttachmentStorageUsage;

    @Column(nullable = false)
    private UserStatus status;

    private OffsetDateTime lastInvitationSent;

    private boolean emailVerified;

    @OneToMany(mappedBy = "user")
    private Set<Project> projects;

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split(","))
                .map(r -> new SimpleGrantedAuthority(r.trim()))
                .toList();
    }

    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void setAccumulatedAttachmentStorageUsage(long accumulatedAttachmentStorageUsage){
        this.accumulatedAttachmentStorageUsage = accumulatedAttachmentStorageUsage < 0 ? 0 : accumulatedAttachmentStorageUsage;
    }
}
