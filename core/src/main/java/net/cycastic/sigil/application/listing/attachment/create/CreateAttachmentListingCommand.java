package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.application.partition.validation.PartitionChecksum;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.listing.AttachmentUploadDto;
import jakarta.annotation.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateAttachmentListingCommand extends AttachmentUploadDto implements Command<AttachmentPresignedDto>, PartitionChecksum {
    @Nullable
    @Base64String
    private String keyMd5;

    @Override
    public String getPartitionChecksum() {
        return keyMd5;
    }

    @Override
    public boolean isMd5() {
        return true;
    }
}
