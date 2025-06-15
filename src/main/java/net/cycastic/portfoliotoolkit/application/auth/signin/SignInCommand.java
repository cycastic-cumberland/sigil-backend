package net.cycastic.portfoliotoolkit.application.auth.signin;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;

public record SignInCommand(String email, String password) implements Command<CredentialDto> { }
