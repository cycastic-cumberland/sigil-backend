package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.ephemeral.GetEphemeralPublicKeyCommand;
import net.cycastic.sigil.application.user.get.GetEnvelopCommand;
import net.cycastic.sigil.application.user.get.GetKdfSettingsCommand;
import net.cycastic.sigil.application.user.get.GetUserCommand;
import net.cycastic.sigil.application.user.invalidatesessions.InvalidateAllSessionsCommand;
import net.cycastic.sigil.application.user.keyring.QueryKeyringCommand;
import net.cycastic.sigil.application.user.refresh.RefreshTokenCommand;
import net.cycastic.sigil.application.user.register.CompleteUserRegistrationCommand;
import net.cycastic.sigil.application.user.register.ProbeRegistrationInvitationCommand;
import net.cycastic.sigil.application.user.register.RegisterUserCommand;
import net.cycastic.sigil.application.user.register.ResendConfirmationEmailCommand;
import net.cycastic.sigil.application.user.self.GetSelfCommand;
import net.cycastic.sigil.application.user.signin.SignInCommand;
import net.cycastic.sigil.application.user.webauthn.enroll.EnrollWebAuthnEnvelopCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.dto.auth.*;
import net.cycastic.sigil.domain.dto.auth.KdfDetailsDto;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.keyring.KeyringDto;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private static final String CACHE_KEY = "AuthController";
    private final Pipelinr pipelinr;

    @PostMapping("register")
    public void registerUser(@Valid @RequestBody RegisterUserCommand command){
        pipelinr.send(command);
    }

    @PostMapping("register/resend")
    public void resendInvitation(@Valid @RequestBody ResendConfirmationEmailCommand command){
        pipelinr.send(command);
    }

    @GetMapping("register/complete")
    public UserInvitationProbeResultDto probeInvitation(@Valid ProbeRegistrationInvitationCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("register/complete")
    public void completeRegistration(@Valid CompleteUserRegistrationParams queryParams,
                                     @Valid @RequestBody CompleteUserRegistrationForm form){
        pipelinr.send(CompleteUserRegistrationCommand.builder()
                        .queryParams(queryParams)
                        .form(form)
                .build());
    }

    @PostMapping
    public @NotNull CredentialDto signIn(@Valid @RequestBody @NotNull SignInCommand command){
        return pipelinr.send(command);
    }

    @PutMapping
    public @NotNull CredentialDto refreshToken(@Valid @RequestBody @NotNull RefreshTokenCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("invalidate")
    public void invalidateAllSessions(@Valid @RequestBody @NotNull InvalidateAllSessionsCommand command){
        pipelinr.send(command);
    }

    @GetMapping("self")
    @CachePut(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'self'+ '?email=' + #result.email")
    public UserDto getSelf(){
        return pipelinr.send(GetSelfCommand.INSTANCE);
    }

    @PostMapping("webauthn/enroll")
    public void enrollWebAuthnKey(@Valid @RequestBody EnrollWebAuthnEnvelopCommand command){
        pipelinr.send(command);
    }

    @GetMapping("kdf")
    @Cacheable(value = CacheConfigurations.Presets.LONG_LIVE_CACHE, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'AuthController::getKdfSettings'+ '?command=' + #command")
    public KdfDetailsDto getKdfSettings(@Valid GetKdfSettingsCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("envelop")
    @Deprecated
    public EnvelopDto getEnvelop(){
        return pipelinr.send(GetEnvelopCommand.INSTANCE);
    }

    @GetMapping("keyring")
    public KeyringDto getKeyring(){
        return pipelinr.send(new QueryKeyringCommand());
    }

    @GetMapping
    @RequireTenantId
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'self'+ '?command=' + #command")
    public UserDto getUser(@Valid GetUserCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("public-key")
    public PemDto getEphemeralPublicKey(){
        return pipelinr.send(GetEphemeralPublicKeyCommand.INSTANCE);
    }
}
