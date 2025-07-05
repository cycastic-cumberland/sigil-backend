package net.cycastic.sigil.application.auth.refresh;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.CredentialDto;

public record RefreshTokenCommand(String authToken) implements Command<CredentialDto> { }
