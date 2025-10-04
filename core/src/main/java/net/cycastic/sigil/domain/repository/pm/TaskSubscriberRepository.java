package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.TaskSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskSubscriberRepository extends JpaRepository<TaskSubscriber, Long> {
    void removeByTask_IdAndSubscriber_Id(int taskId, int subscriberId);
}
