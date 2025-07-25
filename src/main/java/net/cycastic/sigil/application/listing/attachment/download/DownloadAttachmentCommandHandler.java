package net.cycastic.sigil.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.cipher.CipherService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.service.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class DownloadAttachmentCommandHandler implements Command.Handler<DownloadAttachmentCommand, InputStreamResponse> {
    private final AsymmetricDecryptionProvider asymmetricDecryptionProvider;
    private final AttachmentListingRepository attachmentListingRepository;
    private final PartitionRepository partitionRepository;
    private final StorageProvider storageProvider;
    private final CipherService cipherService;

    @Override
    public InputStreamResponse handle(DownloadAttachmentCommand command) {
        var listing = attachmentListingRepository.findById(command.getListingId())
                .orElseThrow(RequestException::forbidden);
        var partition = partitionRepository.findByAttachmentListing(listing);
        var serverCipher = partition.getServerPartitionKey();
        if (serverCipher == null){
            throw RequestException.withExceptionCode("C400T008");
        }
        var unwrappedKey = asymmetricDecryptionProvider.decrypt(command.getEncryptionKey());
        var partitionKey = Base64.getDecoder().decode(unwrappedKey);
        var serverKey = cipherService.unwrapServerManagedKey(serverCipher);
        partitionKey = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, partitionKey, serverKey);
        var partitionKeyChecksum = CryptographicUtilities.digestMd5(partitionKey);
        if (!Arrays.equals(partition.getKeyMd5Digest(), partitionKeyChecksum)){
            throw RequestException.withExceptionCode("C400T006");
        }

        final var decryptionKeyBase64 = partitionKey;
        final var contentLength = listing.getContentLength();
        final var mimeType = listing.getMimeType();
        final var fileName = command.getFileName();
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
                                decryptionKeyBase64);
            }
        };
    }
}
