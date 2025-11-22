package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.TaskProgress;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {
    List<TaskProgress> findByFromStatusInOrNextStatusIn(Collection<TaskStatus> fromStatuses, Collection<TaskStatus> nextStatuses);
    Optional<TaskProgress> findByFromStatusAndNextStatus(TaskStatus fromStatus, TaskStatus nextStatus);
    void deleteByFromStatus_IdAndNextStatus_Id(long fromStatusId, long nextStatusId);
}
