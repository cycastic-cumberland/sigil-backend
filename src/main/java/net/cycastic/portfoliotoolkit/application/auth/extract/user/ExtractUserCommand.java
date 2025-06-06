package net.cycastic.portfoliotoolkit.application.auth.extract.user;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.domain.model.User;

public record ExtractUserCommand(String authToken) implements Command<User> { }
