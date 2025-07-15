package net.cycastic.sigil.domain.model.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.sigil.domain.model.Cipher;
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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String normalizedEmail;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String roles;

    @NotNull
    private OffsetDateTime joinedAt;

    @NotNull
    @Column(columnDefinition = "BINARY(32)")
    private byte[] securityStamp;

    @Version
    private long version;

    @Column(nullable = false)
    private UserStatus status;

    private OffsetDateTime lastInvitationSent;

    private boolean emailVerified;

    @OneToMany(mappedBy = "user")
    private Set<TenantUser> tenantUsers;

    @Column(columnDefinition = "VARBINARY(512)")
    private byte[] publicRsaKey;

    @Column(columnDefinition = "VARBINARY(32)")
    private byte[] kdfSettings;

    @Column(columnDefinition = "VARBINARY(32)")
    private byte[] kdfSalt;

    @OneToOne
    @JoinColumn(name = "wrapped_user_key_id")
    private Cipher wrappedUserKey;

    @OneToOne
    @JoinColumn(name = "webauthn_key_id")
    private Cipher webAuthnKey;

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
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
