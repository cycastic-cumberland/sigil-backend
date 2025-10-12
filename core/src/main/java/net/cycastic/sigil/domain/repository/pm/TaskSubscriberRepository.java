package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskSubscriber;
import net.cycastic.sigil.domain.model.tenant.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface TaskSubscriberRepository extends JpaRepository<TaskSubscriber, Long> {
    @Query("SELECT ts.subscriber FROM TaskSubscriber ts WHERE ts.task = :task AND ts.subscriber IN :users")
    Collection<User> getFilteredSubscribedUsers(@Param("users") Collection<User> users, @Param("task") Task task);
}
