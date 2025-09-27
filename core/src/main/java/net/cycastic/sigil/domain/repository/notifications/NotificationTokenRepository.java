package net.cycastic.sigil.domain.repository.notifications;

import net.cycastic.sigil.domain.model.notification.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, UUID> {
}
