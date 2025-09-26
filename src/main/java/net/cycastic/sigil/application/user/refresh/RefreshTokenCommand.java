package net.cycastic.sigil.application.user.refresh;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

public record RefreshTokenCommand(@NotBlank String authToken) implements Command<CredentialDto> { }
