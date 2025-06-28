package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.application.storage.serve.ServeFileCommand;
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
@RequestMapping("api/storage")
public class StorageController {
    private final Pipelinr pipelinr;

    @GetMapping
    @SneakyThrows
    public ResponseEntity<InputStreamResource> serveFile(ServeFileCommand command){
        var response = pipelinr.send(command);
        var mimeType = response.mimeType() == null ? "application/octet-stream" : response.mimeType();
        var streamSource = response.streamSource();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("attachment", response.fileName());
        headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(streamSource));
    }
}
