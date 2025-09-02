package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.KanbanBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KanbanBoardRepository extends JpaRepository<KanbanBoard, Integer> {
    Optional<KanbanBoard> findByIdAndProjectPartition_Id(int id, int projectPartitionId);
}
