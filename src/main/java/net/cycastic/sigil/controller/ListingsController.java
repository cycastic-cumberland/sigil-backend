package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.acp.create.SaveLACPCommand;
import net.cycastic.sigil.application.listing.attachment.create.CompleteAttachmentUploadCommand;
import net.cycastic.sigil.application.listing.attachment.create.CreateAttachmentListingCommand;
import net.cycastic.sigil.application.listing.delete.DeleteListingCommand;
import net.cycastic.sigil.application.listing.attachment.download.GenerateAttachmentPresignedDownloadCommand;
import net.cycastic.sigil.application.listing.get.GetListingCommand;
import net.cycastic.sigil.application.listing.query.QuerySingleLevelCommand;
import net.cycastic.sigil.controller.annotation.RequireProjectId;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireProjectId
@RequiredArgsConstructor
@RequestMapping("api/listings")
public class ListingsController {
    private final Pipelinr pipelinr;
    @PostMapping("acp")
    public void saveLACP(@RequestBody SaveLACPCommand command){
        pipelinr.send(command);
    }

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
    public AttachmentPresignedDto downloadAttachment(GenerateAttachmentPresignedDownloadCommand command){
        return pipelinr.send(command);
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
