package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.acp.create.SaveLACPCommand;
import net.cycastic.portfoliotoolkit.application.listing.create.attachment.CompleteAttachmentUploadCommand;
import net.cycastic.portfoliotoolkit.application.listing.create.attachment.CreateAttachmentListingCommand;
import net.cycastic.portfoliotoolkit.application.listing.query.QuerySubfoldersCommand;
import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;
import net.cycastic.portfoliotoolkit.domain.dto.SubfoldersDto;
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
    public AttachmentPresignedUploadDto uploadAttachment(@RequestBody CreateAttachmentListingCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("attachment/complete")
    public void completeAttachmentUpload(@RequestBody CompleteAttachmentUploadCommand command){
        pipelinr.send(command);
    }

    @GetMapping("subfolders")
    public SubfoldersDto getSubfolders(QuerySubfoldersCommand command){
        return pipelinr.send(command);
    }
}
