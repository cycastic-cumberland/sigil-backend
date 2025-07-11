package net.cycastic.sigil.application.listing.attachment.presign;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import net.cycastic.sigil.service.UrlAccessor;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GenerateAttachmentPresignedDownloadCommandHandler implements Command.Handler<GenerateAttachmentPresignedDownloadCommand, AttachmentPresignedDto> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;
    private final StorageProvider storageProvider;
    private final PartitionService partitionService;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UrlAccessor urlAccessor;
    private final UriPresigner uriPresigner;

    private AttachmentPresignedDto handleDirect(GenerateAttachmentPresignedDownloadCommand command){
        var partition = partitionService.getPartition();
        var listing = attachmentListingRepository.findByListing_PartitionAndListing_ListingPath(partition, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));

        if (partition.getServerPartitionKey() != null){
            throw RequestException.withExceptionCode("C400T005");
        }

        var submittedMd5 = Base64.getDecoder().decode(command.getKeyMd5());
        if (!Arrays.equals(submittedMd5, partition.getKeyMd5Digest())){
            throw RequestException.withExceptionCode("C400T006");
        }

        var url = storageProvider.getBucket(listing.getBucketName())
                .generatePresignedDownloadPath(listing.getObjectKey(),
                        new File(command.getListingPath()).getName(),
                        OffsetDateTime.now().plusMinutes(2),
                        command.getKeyMd5());
        return AttachmentPresignedDto.builder()
                .id(listing.getId())
                .url(url)
                .build();
    }

    private AttachmentPresignedDto handleServerSide(GenerateAttachmentPresignedDownloadCommand command){
        var partition = partitionService.getPartition();
        var attachmentListing = attachmentListingRepository.findByListing_PartitionAndListing_ListingPath(partition, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var listing = listingRepository.findByAttachmentListing(attachmentListing);

        if (partition.getServerPartitionKey() == null){
            throw RequestException.withExceptionCode("C400T008");
        }

        var wrappedPartitionKey = loggedUserAccessor.tryGetEncryptionKey()
                .orElseThrow(() -> RequestException.withExceptionCode("C400T009"));
        var nvb = OffsetDateTime.now();
        var nva = nvb.plusMinutes(2);
        var downloadUrl = UriComponentsBuilder.fromUriString(urlAccessor.getBackendOrigin())
                .path("/api/presigned/download")
                .queryParam("listingId", listing.getId())
                .queryParam("encryptionKey", ApplicationUtilities.encodeURIComponent(wrappedPartitionKey))
                .queryParam("fileName", ApplicationUtilities.encodeURIComponent(new File(listing.getListingPath()).getName()))
                .queryParam("notValidBefore", nvb.toInstant().getEpochSecond())
                .queryParam("notValidAfter", nva.toInstant().getEpochSecond())
                .build(true)
                .toUri();
        var signedDownloadUrl = uriPresigner.signUri(downloadUrl);
        return AttachmentPresignedDto.builder()
                .id(listing.getId())
                .url(signedDownloadUrl.toString())
                .build();
    }

    @Override
    public AttachmentPresignedDto handle(GenerateAttachmentPresignedDownloadCommand command) {
        return switch (command.getPresignType()){
            case DIRECT_ENCRYPTED -> handleDirect(command);
            case SERVER_SIDE_KEY_DERIVATION -> handleServerSide(command);
        };
    }
}
