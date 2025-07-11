package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CreateAttachmentListingCommandHandler implements Command.Handler<CreateAttachmentListingCommand, AttachmentPresignedDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final PartitionService partitionService;

    @Override
    public AttachmentPresignedDto handle(CreateAttachmentListingCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);
        var partition = partitionService.getPartition();
        if (partition.getServerPartitionKey() != null){
            throw RequestException.withExceptionCode("C400T005");
        }

        var submittedMd5 = Base64.getDecoder().decode(command.getKeyMd5());
        if (!Arrays.equals(submittedMd5, partition.getKeyMd5Digest())){
            throw RequestException.withExceptionCode("C400T006");
        }

        var path = command.getPath();
        var incompleteAttachment = listingService.saveTemporaryAttachment(partition,
                path,
                command.getMimeType() != null ? command.getMimeType() : ApplicationUtilities.getMimeType(command.getPath()),
                command.getContentLength());

        var uploadUrl = storageProvider.getBucket(incompleteAttachment.getBucketName())
                .generatePresignedUploadPath(incompleteAttachment.getObjectKey(),
                        path,
                        OffsetDateTime.now().plusMinutes(2),
                        command.getContentLength(),
                        submittedMd5);
        return AttachmentPresignedDto.builder()
                .id(incompleteAttachment.getId())
                .url(uploadUrl)
                .build();
    }
}
