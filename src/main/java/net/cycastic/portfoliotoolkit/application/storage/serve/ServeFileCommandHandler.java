package net.cycastic.portfoliotoolkit.application.storage.serve;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import net.cycastic.portfoliotoolkit.service.UrlAccessor;
import net.cycastic.portfoliotoolkit.service.impl.UriPresigner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class ServeFileCommandHandler implements Command.Handler<ServeFileCommand, ServeFileCommandResponse> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final StorageProvider storageProvider;
    private final UriPresigner uriPresigner;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public ServeFileCommandResponse handle(ServeFileCommand command) {
        var url = loggedUserAccessor.getRequestPath();
        if (!uriPresigner.verifyUri(URI.create(url))){
            throw new RequestException(401, "Signature verification failed");
        }

        var listing = attachmentListingRepository.findByListing_Project_IdAndListing_ListingPath(command.getProjectId(), command.getPath())
                .orElseThrow(() -> new RequestException(404, "Attachment not found"));
        if (!listing.getShareToken().equals(command.getShareToken())){
            throw new RequestException(404, "Attachment not found");
        }

        final var bucketName = listing.getBucketName();
        final var objectKey = listing.getObjectKey();
        final var store = storageProvider;

        return new ServeFileCommandResponse(() -> store.getBucket(bucketName).openDownloadStream(objectKey),
                new File(command.getPath()).getName(),
                listing.getMimeType());
    }
}
