package net.cycastic.sigil.service.cleaners;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.CleanerConfigurations;
import net.cycastic.sigil.domain.model.ListingType;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.BackgroundCleaner;
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
