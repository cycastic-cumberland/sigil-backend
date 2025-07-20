package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.tenant.User;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "webauthn_credentials")
public class WebAuthnCredential {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private User user;

    @Column(columnDefinition = "VARBINARY(64)", nullable = false)
    private byte[] credentialId;

    @Column(columnDefinition = "VARBINARY(64)", nullable = false)
    private byte[] salt;

    @Column(nullable = false)
    private String transports;

    @OneToOne
    @JoinColumn(name = "wrapped_user_key_id",  nullable = false)
    private Cipher wrappedUserKey;
}
