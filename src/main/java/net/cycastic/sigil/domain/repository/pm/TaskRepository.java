package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    Optional<Task> findByTenant_IdAndTaskIdentifier(int tenantId, String taskIdentifier);

    Page<Task> findByTenant_IdAndTaskStatus_Id(int tenantId, Long taskStatusId, Pageable pageable);
}
