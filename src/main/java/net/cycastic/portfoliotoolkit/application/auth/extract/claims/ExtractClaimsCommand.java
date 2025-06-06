package net.cycastic.portfoliotoolkit.application.auth.extract.claims;

import an.awesome.pipelinr.Command;
import io.jsonwebtoken.Claims;

public record ExtractClaimsCommand(String authToken) implements Command<Claims> { }
