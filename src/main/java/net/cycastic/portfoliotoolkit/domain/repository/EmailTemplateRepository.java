package net.cycastic.portfoliotoolkit.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.EmailTemplate;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
    Optional<EmailTemplate> findByAttachmentListing_Listing_ProjectAndAttachmentListing_Listing_ListingPath(@NotNull Project attachmentListingListingProject, @NotNull String attachmentListingListingListingPath);
}
