package net.cycastic.sigil.domain.model.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.tenant.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_settings", indexes = {@Index(name = "notification_settings_user_id_notification_type", columnList = "user_id,notification_type", unique = true)})
public class NotificationSetting {
    @Id
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "VARCHAR(64)", nullable = false)
    private String notificationType;

    private boolean notificationDisabled;
}
