package net.cycastic.sigil.domain;

import net.cycastic.sigil.service.InputStreamResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

public class WebUtilities {
    public static ResponseEntity<InputStreamResource> toResponse(InputStreamResponse response, Duration cacheDuration){
        var headers = new HttpHeaders();
        var mimeType = response.getMimeType() == null ? "application/octet-stream" : response.getMimeType();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        if (response.getFileName() != null) {
            headers.setContentDispositionFormData("attachment", response.getFileName());
        }
        if (response.getContentLength() != null){
            headers.setContentLength(response.getContentLength());
        }
        headers.setCacheControl(CacheControl.maxAge(cacheDuration));
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(response));
    }

    public static ResponseEntity<InputStreamResource> toResponse(InputStreamResponse response){
        return toResponse(response, Duration.ofHours(1));
    }
}
