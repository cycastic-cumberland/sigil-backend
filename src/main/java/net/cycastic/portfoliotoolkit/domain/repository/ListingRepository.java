package net.cycastic.portfoliotoolkit.domain.repository;

import net.cycastic.portfoliotoolkit.domain.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Integer> {
}
