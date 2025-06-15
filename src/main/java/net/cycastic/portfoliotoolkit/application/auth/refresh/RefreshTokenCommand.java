package net.cycastic.portfoliotoolkit.application.auth.refresh;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;

public record RefreshTokenCommand(String authToken) implements Command<CredentialDto> { }
