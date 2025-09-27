package net.cycastic.sigil.application.listing.attachment.upload;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.cipher.CipherService;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.StorageProvider;
import net.cycastic.sigil.service.impl.EncryptionKeyHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UploadAttachmentCommandHandler implements Command.Handler<UploadAttachmentCommand, Void> {
    private final StorageProvider storageProvider;
    private final ListingService listingService;
    private final PartitionService partitionService;
    private final CipherService cipherService;
    private final EncryptionKeyHelper encryptionKeyHelper;

    @Override
    public Void handle(UploadAttachmentCommand command) {
        var partition = partitionService.getPartition();
        var serverCipher = partition.getServerPartitionKey();
        if (serverCipher == null){
            throw RequestException.withExceptionCode("C400T008");
        }
        var partitionKey = encryptionKeyHelper.getPartitionKey();
        var serverKey = cipherService.unwrapServerManagedKey(serverCipher);
        partitionKey = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, partitionKey, serverKey);
        var partitionKeyChecksum = CryptographicUtilities.digestMd5(partitionKey);
        if (!Arrays.equals(partition.getKeyMd5Digest(), partitionKeyChecksum)){
            throw RequestException.withExceptionCode("C400T006");
        }

        var path = command.getPath();
        var incompleteAttachment = listingService.saveTemporaryAttachment(partition,
                path,
                command.getMimeType(),
                command.getContentLength());
        storageProvider.getBucket(incompleteAttachment.getBucketName())
                .upload(incompleteAttachment.getObjectKey(),
                        command.getMimeType(),
                        command.getContentLength(),
                        command.getFileUpload()::openReader,
                        partitionKey);
        listingService.markAttachmentUploadAsCompleted(incompleteAttachment, false);
        return null;
    }
}
