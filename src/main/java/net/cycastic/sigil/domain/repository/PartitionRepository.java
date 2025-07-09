package net.cycastic.sigil.domain.repository;

import net.cycastic.sigil.domain.model.Partition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartitionRepository extends JpaRepository<Partition, Integer> {
}
