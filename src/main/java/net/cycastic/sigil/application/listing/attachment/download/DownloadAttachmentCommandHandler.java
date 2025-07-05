package net.cycastic.sigil.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.service.InputStreamResponse;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class DownloadAttachmentCommandHandler implements Command.Handler<DownloadAttachmentCommand, InputStreamResponse> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final AttachmentListingRepository attachmentListingRepository;
    private final StorageProvider storageProvider;
    @Override
    public InputStreamResponse handle(final DownloadAttachmentCommand command) {
        if (command.getEncryptionKeyBase64() == null){
            throw new RequestException(400, "Directly downloading non-encrypted attachments is not supported");
        }

        var project = tenantRepository.findById(loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));

        var listing = attachmentListingRepository.findByListing_TenantAndListing_ListingPath(project, command.getListingPath())
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
