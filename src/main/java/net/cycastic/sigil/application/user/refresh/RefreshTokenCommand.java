package net.cycastic.sigil.application.user.refresh;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

public record RefreshTokenCommand(@NotEmpty String authToken) implements Command<CredentialDto> { }
