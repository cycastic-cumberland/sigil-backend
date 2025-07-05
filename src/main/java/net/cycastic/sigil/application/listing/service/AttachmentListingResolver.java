package net.cycastic.sigil.application.listing.service;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.ListingType;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.dto.listing.AttachmentDto;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Lazy
@Component
@RequiredArgsConstructor
public class AttachmentListingResolver implements ListingResolver {
    private final AttachmentListingRepository repository;

    @Override
    public void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector) {
        var ids = listings.values().stream()
                .filter(l -> l.getType() == ListingType.ATTACHMENT)
                .map(Listing::getId)
                .toList();
        var filteredListings = repository.findAllById(ids);
        for (var l : filteredListings){
            collector.put(l.getId(), AttachmentDto.fromDomain(l, listings.get(l.getId())));
        }
    }

    @Override
    public Optional<ListingDto> resolve(Listing listing) {
        if (listing.getType() != ListingType.ATTACHMENT){
            return Optional.empty();
        }

        var attachmentListing = listing.getAttachmentListing();
        if (!attachmentListing.isUploadCompleted()){
            throw new RequestException(400, "Attachment is uploading or corrupted");
        }
        return Optional.of(AttachmentDto.fromDomain(attachmentListing, listing));
    }
}
