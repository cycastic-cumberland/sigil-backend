package net.cycastic.sigil.application.user.refresh;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

public record RefreshTokenCommand(String authToken) implements Command<CredentialDto> { }
