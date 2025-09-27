package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.listing.attachment.create.CompleteAttachmentUploadCommand;
import net.cycastic.sigil.application.listing.attachment.create.CreateAttachmentListingCommand;
import net.cycastic.sigil.application.listing.attachment.get.GetAttachmentCommand;
import net.cycastic.sigil.application.listing.attachment.upload.FileUpload;
import net.cycastic.sigil.application.listing.attachment.upload.UploadAttachmentCommand;
import net.cycastic.sigil.application.listing.delete.DeleteListingCommand;
import net.cycastic.sigil.application.listing.attachment.presign.GenerateAttachmentPresignedDownloadCommand;
import net.cycastic.sigil.application.listing.get.GetListingCommand;
import net.cycastic.sigil.application.listing.query.QueryListingSingleLevelCommand;
import net.cycastic.sigil.application.listing.update.UpdateListingCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.controller.annotation.RequireEncryptionKey;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.listing.AttachmentDto;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/listings")
public class ListingsController {
    private final Pipelinr pipelinr;

    @DeleteMapping
    public void deleteListing(@Valid DeleteListingCommand command){
        pipelinr.send(command);
    }

    @PatchMapping
    public void updateListing(@Valid UpdateListingCommand command){
        pipelinr.send(command);
    }

    @GetMapping("attachment")
    public AttachmentDto getAttachment(@Valid GetAttachmentCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("attachment")
    @RequireEncryptionKey
    public void uploadAttachment(@RequestParam("listingPath") String listingPath, final @RequestParam("file") MultipartFile file){
        var command = new UploadAttachmentCommand();
        command.setPath(listingPath);
        command.setMimeType(Objects.requireNonNullElse(Objects.requireNonNullElseGet(file.getContentType(), () -> ApplicationUtilities.getMimeType(file.getOriginalFilename())),
                "application/octet-stream"));
        command.setContentLength(file.getSize());
        command.setFileUpload(new FileUpload() {
            @Override
            @SneakyThrows
            public InputStream openReader() {
                return file.getInputStream();
            }
        });
        pipelinr.send(command);
    }

    @PostMapping("attachment/presigned")
    public AttachmentPresignedDto generatePresignedAttachmentUpload(@Valid @RequestBody CreateAttachmentListingCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("attachment/complete")
    public void completeAttachmentUpload(@Valid @RequestBody CompleteAttachmentUploadCommand command){
        pipelinr.send(command);
    }

    @GetMapping("attachment/presigned")
    public AttachmentPresignedDto generatePresignedAttachmentDownload(@Valid GenerateAttachmentPresignedDownloadCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("subfolders")
    public PageResponseDto<FolderItemDto> getSubfolders(@Valid QueryListingSingleLevelCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("listing")
    public ListingDto getListing(@Valid GetListingCommand command){
        return pipelinr.send(command);
    }
}
