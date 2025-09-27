package net.cycastic.sigil.domain.repository.notifications;

import net.cycastic.sigil.domain.model.notification.NotificationSetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Integer> {
    List<NotificationSetting> findByUser_Id(int userId, Pageable pageable);

    @Query("""
           SELECT ns FROM NotificationSetting ns WHERE ns.user.id = :userId AND ns.notificationDisabled
           """)
    Collection<NotificationSetting> getDisabled(@Param("userId") int userId);
}
