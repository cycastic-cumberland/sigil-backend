package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.attachment.download.DownloadAttachmentCommand;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/presigned")
public class PresignedController {
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
}
