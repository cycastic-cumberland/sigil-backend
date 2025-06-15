package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.email.smtp.delete.DeleteSmtpCredentialCommand;
import net.cycastic.portfoliotoolkit.application.email.smtp.get.GetSmtpCredentialCommand;
import net.cycastic.portfoliotoolkit.application.email.smtp.query.QuerySmtpCredentialsCommand;
import net.cycastic.portfoliotoolkit.application.email.smtp.save.SaveSmtpCredentialCommand;
import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import net.cycastic.portfoliotoolkit.dto.BaseSmtpCredentialDto;
import net.cycastic.portfoliotoolkit.dto.DecryptedSmtpCredentialDto;
import net.cycastic.portfoliotoolkit.dto.IdDto;
import net.cycastic.portfoliotoolkit.dto.paging.PageResponseDto;
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
    public IdDto saveCredential(SaveSmtpCredentialCommand command){
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
}
