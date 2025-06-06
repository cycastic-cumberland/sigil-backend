package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.auth.refresh.RefreshTokenCommand;
import net.cycastic.portfoliotoolkit.application.auth.signin.SignInCommand;
import net.cycastic.portfoliotoolkit.dto.CredentialDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private final Pipelinr pipelinr;

    @PostMapping
    public @NotNull CredentialDto signIn(@RequestBody @NotNull SignInCommand command){
        return pipelinr.send(command);
    }

    @PutMapping
    public @NotNull CredentialDto refreshToken(@RequestBody @NotNull RefreshTokenCommand command){
        return pipelinr.send(command);
    }
}
