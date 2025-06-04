package net.cycastic.portfoliotoolkit.controller;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequireProjectId
@RequiredArgsConstructor
@RequestMapping("api/hello")
public class HelloController {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;

    @GetMapping("/example")
    public Collection<User> example() {
        return userRepository.findAll();
    }
}
