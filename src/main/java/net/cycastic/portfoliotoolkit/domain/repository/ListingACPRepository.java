package net.cycastic.portfoliotoolkit.domain.repository;

import net.cycastic.portfoliotoolkit.domain.model.ListingAccessControlPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingACPRepository extends JpaRepository<ListingAccessControlPolicy, Integer> {
}
