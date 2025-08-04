package net.cycastic.sigil.application.listing.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.configuration.S3Configurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.ListingType;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.*;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.DeferrableStorageProvider;
import net.cycastic.sigil.service.LimitProvider;
import net.cycastic.sigil.service.StorageProvider;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingService {
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final TenantRepository tenantRepository;
    private final List<ListingResolver> resolvers;
    private final StorageProvider storageProvider;
    private final DeferrableStorageProvider deferrableStorageProvider;
    private final LimitProvider limitProvider;

    private final PartitionService partitionService;
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;
    private final S3Configurations s3Configurations;

    public ListingDto toDto(Listing listing){
        for (var resolver : resolvers){
            var opt = resolver.resolve(listing);
            if (opt.isPresent()){
                return opt.get();
            }
        }

        // TODO: provide more details
        throw new RequestException(400, "Listing is broken");
    }

    public PageResponseDto<ListingDto> toDto(Page<Listing> listings){
        var domainMap = listings.getContent().stream()
                .collect(Collectors.toMap(Listing::getId, l -> l));
        HashMap<Integer, ListingDto> dtoMap = HashMap.newHashMap(domainMap.size());
        for (var resolver : resolvers){
            resolver.resolve(domainMap, dtoMap);
            if (dtoMap.size() == domainMap.size()){
                break;
            }
        }

        if (dtoMap.size() != domainMap.size()){
            // TODO: provide more details
            logger.error("Listing are broken");
        }

        var items = listings.getContent().stream()
                .map(l -> dtoMap.get(l.getId()))
                .toList();
        PageResponseDto.PageResponseDtoBuilder<ListingDto> builder = PageResponseDto.builder();
        builder.items(items)
                .page(listings.getNumber() + 1)
                .pageSize(listings.getSize())
                .totalPages(listings.getTotalPages())
                .totalElements((int)listings.getTotalElements());

        return builder.build();
    }

    @Transactional
    public AttachmentListing saveTemporaryAttachment(@NotNull Partition partition, @NotNull String path, String mimeType, long contentLength){
        var listing = Listing.builder()
                .partition(partition)
                .listingPath(path)
                .type(ListingType.ATTACHMENT)
                .createdAt(OffsetDateTime.now())
                .build();
        var fileExt = FilenameUtils.getExtension(path);
        var objectUuid = fileExt.isEmpty() ? UUID.randomUUID().toString() : String.format("%s.%s", UUID.randomUUID(), fileExt);
        objectUuid = ApplicationUtilities.shardObjectKey(objectUuid);
        var attachmentListing = AttachmentListing.builder()
                .listing(listing)
                .bucketName(s3Configurations.getAttachmentBucketName())
                .bucketRegion(s3Configurations.getRegionName())
                .objectKey(objectUuid)
                .mimeType(mimeType)
                .contentLength(contentLength)
                .build();
        listingRepository.save(listing);
        attachmentListingRepository.save(attachmentListing);
        return attachmentListing;
    }

    private void deleteListingNoTransaction(Listing listing, AttachmentListing attachment){
        if (attachment.isUploadCompleted()){
            attachment.setUploadCompleted(false);
            attachmentListingRepository.save(attachment);

            var size = attachment.getContentLength();
            var project = listing.getPartition().getTenant();
            project.setAccumulatedAttachmentStorageUsage(project.getAccumulatedAttachmentStorageUsage() - size);
            deferrableStorageProvider.getBucket(attachment.getBucketName()).deleteFile(attachment.getObjectKey());
            tenantRepository.save(project);
        }

        attachmentListingRepository.delete(attachment);
        listingRepository.delete(listing);
    }

    public void deleteListingNoTransaction(String path){
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);
        var partition = partitionService.getPartition();
        var listing = listingRepository.findByPartitionAndListingPath(partition, path)
                .orElseThrow(() -> new RequestException(404, "Listing not found"));

        switch (listing.getType()){
            case ATTACHMENT -> {
                var attachment = attachmentListingRepository.findAttachmentListingForUpdate(listing)
                        .orElseThrow(() -> new IllegalStateException("unreachable"));
                deleteListingNoTransaction(listing, attachment);
            }
        }
    }

    @Transactional
    public void markAttachmentUploadAsCompleted(AttachmentListing listing, boolean performCheck){
        var tenant = tenantRepository.findByAttachmentListing(listing)
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));
        if (listing.isUploadCompleted()){
            return;
        }

        if (performCheck){
            var objectExists = false;
            try {
                objectExists = storageProvider.getBucket(listing.getBucketName()).exists(listing.getObjectKey());
            } catch (software.amazon.awssdk.services.s3.model.S3Exception e){
                if (e.statusCode() != 400){
                    throw new RequestException(e.statusCode(), e, e.getLocalizedMessage());
                }

                objectExists = true;
            }

            if (!objectExists){
                throw RequestException.withExceptionCode("C400T007");
            }
        }

        var size = listing.getContentLength();
        var limit = limitProvider.extractUsageDetails(tenant);
        if (limit.getPerAttachmentSize() != null && size > limit.getPerAttachmentSize()){
            logger.error("File is larger than permitted limit. Size: {} byte(s), limit: {} byte(s)",
                    size, limit.getPerAttachmentSize());
            throw new RequestException(413, "File is larger than permitted limit");
        }
        var acc = tenant.getAccumulatedAttachmentStorageUsage() + size;
        if (limit.getAllAttachmentSize() != null && acc > limit.getAllAttachmentSize()){
            throw new RequestException(413, "Accumulated storage usage exceeded");
        }

        listing.setUploadCompleted(true);
        attachmentListingRepository.save(listing);
        tenant.setAccumulatedAttachmentStorageUsage(acc);
        tenantRepository.save(tenant);
    }
}
