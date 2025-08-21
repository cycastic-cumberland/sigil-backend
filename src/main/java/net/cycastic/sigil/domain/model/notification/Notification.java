package net.cycastic.sigil.domain.model.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.tenant.User;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {@Index(name = "notifications_user_id_created_at_index", columnList = "user_id,created_at")})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    private boolean isRead;

    @Column(columnDefinition = "VARBINARY(2000)", nullable = false)
    private byte[] notificationContent;

    @Column(columnDefinition = "VARCHAR(64)", nullable = false)
    private String notificationType;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToOne
    @JoinColumn(name = "encryption_cipher", nullable = false)
    private Cipher encryptionCipher;
}
