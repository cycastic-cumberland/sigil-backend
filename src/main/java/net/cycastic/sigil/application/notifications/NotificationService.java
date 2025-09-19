package net.cycastic.sigil.application.notifications;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.notification.Notification;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final CipherRepository cipherRepository;
    private final NotificationSender notificationSender;

    @Transactional
    public Notification saveNotification(User user, String notificationType, String notificationContent){
        var key = new byte[CryptographicUtilities.KEY_LENGTH];
        var iv = new byte[CryptographicUtilities.NONCE_LENGTH];
        CryptographicUtilities.generateRandom(key);
        CryptographicUtilities.generateRandom(iv);
        var userPublicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(user.getPublicRsaKey());
        var encryptedKey = CryptographicUtilities.encrypt(userPublicKey, key);
        var encryptionCipher = new Cipher(CipherDecryptionMethod.UNWRAPPED_USER_KEY, iv, encryptedKey.getCipher());
        var secretKey = new SecretKeySpec(key, "AES");
        var encryptedNotificationContent = CryptographicUtilities.encrypt(secretKey, iv, notificationContent.getBytes(StandardCharsets.UTF_8));

        var notification = Notification.builder()
                .user(user)
                .isRead(false)
                .notificationContent(encryptedNotificationContent)
                .notificationType(notificationType)
                .createdAt(OffsetDateTime.now())
                .encryptionCipher(encryptionCipher)
                .build();
        cipherRepository.save(encryptionCipher);
        notificationRepository.save(notification);
        return notification;
    }

    public void sendNotification(UUID uuid, String eventName, Object payload, String prefix){
        notificationSender.sendNotification(prefix + uuid, eventName, payload);
    }

    public void triggerNotification(UUID uuid, String eventName){
        sendNotification(uuid, eventName, Collections.emptyList(), "private-");
    }
}
