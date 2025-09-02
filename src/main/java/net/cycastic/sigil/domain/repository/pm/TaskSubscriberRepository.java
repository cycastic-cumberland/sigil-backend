package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.TaskSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskSubscriberRepository extends JpaRepository<TaskSubscriber, Long> {
}
