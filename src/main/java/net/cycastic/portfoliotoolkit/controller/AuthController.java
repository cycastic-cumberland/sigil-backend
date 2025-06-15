package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.auth.invalidatesessions.InvalidateAllSessionsCommand;
import net.cycastic.portfoliotoolkit.application.auth.refresh.RefreshTokenCommand;
import net.cycastic.portfoliotoolkit.application.auth.self.GetSelfCommand;
import net.cycastic.portfoliotoolkit.application.auth.signin.SignInCommand;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;
import net.cycastic.portfoliotoolkit.domain.dto.UserDto;
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

    @PostMapping("invalidate")
    public void invalidateAllSessions(@RequestBody @NotNull InvalidateAllSessionsCommand command){
        pipelinr.send(command);
    }

    @GetMapping("self")
    public UserDto getSelf(){
        return pipelinr.send(GetSelfCommand.INSTANCE);
    }
}
