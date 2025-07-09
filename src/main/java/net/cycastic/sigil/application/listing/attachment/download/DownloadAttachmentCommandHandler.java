package net.cycastic.sigil.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.service.InputStreamResponse;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class DownloadAttachmentCommandHandler implements Command.Handler<DownloadAttachmentCommand, InputStreamResponse> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final PartitionService partitionService;
    private final StorageProvider storageProvider;

    @Override
    public InputStreamResponse handle(final DownloadAttachmentCommand command) {
        var partition = partitionService.getPartition();

        var listing = attachmentListingRepository.findByListing_PartitionAndListing_ListingPath(partition, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        final var decryptionKey = Base64.getDecoder().decode(command.getEncryptionKeyBase64());
        final var contentLength = listing.getContentLength();
        final var mimeType = listing.getMimeType();
        final var fileName = new File(command.getListingPath()).getName();
        final var objectKey = listing.getObjectKey();
        final var bucketName = listing.getBucketName();

        return new InputStreamResponse() {
            @Override
            public Long getContentLength() {
                return contentLength;
            }

            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public String getMimeType() {
                return mimeType;
            }

            @NonNull
            @Override
            public InputStream getInputStream() {
                return storageProvider.getBucket(bucketName)
                        .download(objectKey,
                                fileName,
                                decryptionKey);
            }
        };
    }
}
