package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.attachment.download.DownloadAttachmentCommand;
import net.cycastic.sigil.application.tenant.members.invite.CompleteTenantInvitationCommand;
import net.cycastic.sigil.application.tenant.members.invite.ProbeTenantInvitationCommand;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationParams;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationProbeResultDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/public")
public class PublicController {
    private final Pipelinr pipelinr;

    @GetMapping("download")
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

    @GetMapping("tenant/complete-invitation")
    public TenantInvitationProbeResultDto probeTenantInvitation(@Valid ProbeTenantInvitationCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("tenant/complete-invitation")
    public void completeTenantInvitation(@Valid TenantInvitationParams queryParams,
                                         @RequestBody CompleteUserRegistrationForm form){
        pipelinr.send(CompleteTenantInvitationCommand.builder()
                        .queryParams(queryParams)
                        .form(form)
                .build());
    }
}
