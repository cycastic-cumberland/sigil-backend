package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.attachment.create.CompleteAttachmentUploadCommand;
import net.cycastic.sigil.application.listing.attachment.create.CreateAttachmentListingCommand;
import net.cycastic.sigil.application.listing.attachment.download.DownloadAttachmentCommand;
import net.cycastic.sigil.application.listing.delete.DeleteListingCommand;
import net.cycastic.sigil.application.listing.attachment.download.GenerateAttachmentPresignedDownloadCommand;
import net.cycastic.sigil.application.listing.get.GetListingCommand;
import net.cycastic.sigil.application.listing.query.QuerySingleLevelCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireProjectId;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequireProjectId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/listings")
public class ListingsController {
    private final Pipelinr pipelinr;

    @PostMapping("attachment")
    public AttachmentPresignedDto uploadAttachment(@RequestBody CreateAttachmentListingCommand command){
        return pipelinr.send(command);
    }

    @DeleteMapping
    public void deleteListing(DeleteListingCommand command){
        pipelinr.send(command);
    }

    @PostMapping("attachment/complete")
    public void completeAttachmentUpload(@RequestBody CompleteAttachmentUploadCommand command){
        pipelinr.send(command);
    }

    @GetMapping("attachment/download")
    public AttachmentPresignedDto generatePresignedAttachmentDownload(GenerateAttachmentPresignedDownloadCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("attachment")
    public ResponseEntity<InputStreamResource> downloadAttachment(DownloadAttachmentCommand command){
        var response = pipelinr.send(command);
        var headers = new HttpHeaders();
        var mimeType = response.getMimeType() == null ? "application/octet-stream" : response.getMimeType();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        if (response.getFileName() != null) {
            headers.setContentDispositionFormData("attachment", response.getFileName());
        }
        if (response.getContentLength() != null){
            headers.setContentLength(response.getContentLength());
        }
        headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(response));
    }

    @GetMapping("subfolders")
    public PageResponseDto<FolderItemDto> getSubfolders(QuerySingleLevelCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("listing")
    public ListingDto getListing(GetListingCommand command){
        return pipelinr.send(command);
    }
}
