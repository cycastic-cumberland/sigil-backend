package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.email.preview.PreviewEmailCommand;
import net.cycastic.sigil.application.email.smtp.delete.DeleteSmtpCredentialCommand;
import net.cycastic.sigil.application.email.smtp.get.GetSmtpCredentialCommand;
import net.cycastic.sigil.application.email.smtp.query.QuerySmtpCredentialsCommand;
import net.cycastic.sigil.application.email.smtp.save.SaveSmtpCredentialCommand;
import net.cycastic.sigil.controller.annotation.RequireProjectId;
import net.cycastic.sigil.domain.dto.BaseSmtpCredentialDto;
import net.cycastic.sigil.domain.dto.DecryptedSmtpCredentialDto;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireProjectId
@RequiredArgsConstructor
@RequestMapping("api/emails")
public class EmailsController {
    private final Pipelinr pipelinr;

    @GetMapping("credentials")
    public PageResponseDto<BaseSmtpCredentialDto> queryCredentials(QuerySmtpCredentialsCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("credential")
    public IdDto saveCredential(@RequestBody SaveSmtpCredentialCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("credential")
    public DecryptedSmtpCredentialDto getCredential(GetSmtpCredentialCommand command){
        return pipelinr.send(command);
    }

    @DeleteMapping("credential")
    public void deleteCredential(DeleteSmtpCredentialCommand command){
        pipelinr.send(command);
    }

    @PostMapping("preview")
    @SneakyThrows
    public ResponseEntity<InputStreamResource> preview(@RequestBody PreviewEmailCommand command){
        var streamSource = pipelinr.send(command);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", "preview.html");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(streamSource));
    }
}
