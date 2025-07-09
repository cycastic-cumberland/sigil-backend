package net.cycastic.sigil.domain.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.model.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    @Nullable User findByNormalizedEmail(@NotNull String normalizedEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Integer id);

    default @Nullable User getByEmail(@NotNull String email){
        return findByNormalizedEmail(email.toUpperCase(Locale.ROOT));
    }

    @Query("select al.listing.partition.tenant.owner from AttachmentListing al WHERE al = :attachmentListing")
    Optional<User> findByAttachmentListing(@Param("attachmentListing") AttachmentListing attachmentListing);

    @Query("select l.partition.tenant.owner from Listing l where l = :listing")
    Optional<User> findByListing(@Param("listing") Listing listing);
}
