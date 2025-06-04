package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;

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

    private boolean disabled;

    @NotNull
    private OffsetDateTime joinedAt;

    @OneToMany(mappedBy = "user")
    private Set<Project> projects;

    @Override
    public boolean isEnabled() {
        return !disabled;
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

    public static User fromAuthentication(@NotNull Authentication authentication){
        return (User)authentication.getPrincipal();
    }
}
