package net.cycastic.sigil.domain.model.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_tokens")
public class NotificationToken {
    @Id
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationTokenConsumer consumer;

    public UUID getToken(){
        return id;
    }

    @PrePersist
    private void onCreate() {
        if (id == null){
            id = UUID.randomUUID();
        }
    }
}
