package net.cycastic.portfoliotoolkit.command;

import net.cycastic.portfoliotoolkit.application.auth.UserService;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "create-user", mixinStandardHelpOptions = true, description = "Create a new user")
public class CreateUser implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CreateUser.class);
    @CommandLine.Option(names = "--email", required = true)
    private String email;

    @CommandLine.Option(names = "--first-name", required = true)
    private String firstName;

    @CommandLine.Option(names = "--last-name", required = true)
    private String lastName;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    @CommandLine.Option(names = "--roles", required = true, split = ",")
    private List<String> roles;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUser(UserRepository userRepository, PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Integer call() {
        var user = User.builder()
                .email(email)
                .normalizedEmail(email.toUpperCase(Locale.ROOT))
                .firstName(firstName)
                .lastName(lastName)
                .hashedPassword(passwordHasher.hash(password))
                .roles(String.join(",", roles))
                .disabled(false)
                .joinedAt(OffsetDateTime.now())
                .securityStamp(new byte[32])
                .build();
        UserService.refreshSecurityStamp(user);
        userRepository.save(user);
        logger.info("User created.");
        return 0;
    }
}
