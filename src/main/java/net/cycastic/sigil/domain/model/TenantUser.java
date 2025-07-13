package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import static net.cycastic.sigil.domain.ApplicationConstants.TenantPermissions.MODERATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenant_users", indexes = @Index(name = "tenant_users_tenant_id_user_id_uindex", columnList = "tenant_id,user_id", unique = true))
public class TenantUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    private int permissions;

    @Formula("permissions & " + MODERATE + " = " + MODERATE)
    private boolean isModerator;
}
