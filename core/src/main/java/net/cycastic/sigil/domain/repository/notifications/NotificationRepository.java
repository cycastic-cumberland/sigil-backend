package net.cycastic.sigil.domain.repository.notifications;

import jakarta.transaction.Transactional;
import net.cycastic.sigil.domain.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
           SELECT n FROM Notification n
           WHERE n.user.id = :userId AND
           n.id > :sinceId AND
           n.isRead IN :readStatuses AND
           n.notificationType NOT IN :ignoreTypes
           ORDER BY n.createdAt LIMIT :amount
           """)
    List<Notification> getNotificationUpper(@Param("userId") int userId,
                                                  @Param("sinceId") Long sinceId,
                                                  @Param("readStatuses") boolean[] readStatuses,
                                                  @Param("ignoreTypes") Collection<String> ignoreTypes,
                                                  @Param("amount") int amount);

    @Query("""
           SELECT n FROM Notification n
           WHERE n.user.id = :userId AND
           n.id < :sinceId AND
           n.isRead IN :readStatuses AND
           n.notificationType NOT IN :ignoreTypes
           ORDER BY n.createdAt DESC LIMIT :amount
           """)
    List<Notification> getNotificationLower(@Param("userId") int userId,
                                            @Param("sinceId") Long sinceId,
                                            @Param("readStatuses") boolean[] readStatuses,
                                            @Param("ignoreTypes") Collection<String> ignoreTypes,
                                            @Param("amount") int amount);

    @Query("""
           SELECT COUNT(1)
           FROM Notification n
           WHERE n.user.id = :userId AND
                 n.isRead = FALSE AND
                 n.notificationType NOT IN :ignoreTypes
           """)
    long countUnreadNotifications(@Param("userId") int userId, @Param("ignoreTypes") Collection<String> ignoreTypes);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.id IN :notificationIds")
    int markAsRead(@Param("userId") int userId, @Param("notificationIds") long[] notificationIds);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.id IN :notificationIds")
    int removeNotifications(@Param("userId") int userId, @Param("notificationIds") long[] notificationIds);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    int removeNotifications(@Param("userId") int userId);
}
