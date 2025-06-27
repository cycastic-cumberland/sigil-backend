package net.cycastic.portfoliotoolkit.service.cleaners;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.configuration.CleanerConfigurations;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.BackgroundCleaner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Lazy
@Component
@RequiredArgsConstructor
public class IncompleteUploadCleanup implements BackgroundCleaner {
    private final CleanerConfigurations cleanerConfigurations;
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;

    @Override
    @Transactional
    public void clean() {
        var threshold = OffsetDateTime.now().minusSeconds(cleanerConfigurations.getIncompleteUploadTtlSeconds());
        attachmentListingRepository.removeByUploadCompletedAndListing_CreatedAtLessThan(false, threshold);
        listingRepository.removeByTypeAndAttachmentListing(ListingType.ATTACHMENT, null);
    }
}
