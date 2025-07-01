package net.cycastic.portfoliotoolkit.command;

import net.cycastic.portfoliotoolkit.application.auth.UserService;
import net.cycastic.portfoliotoolkit.domain.model.UsageType;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.model.UserStatus;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    @CommandLine.Option(names = "--usage-type")
    private UsageType usageType;

    private final UserService userService;

    public CreateUser(UserService userService){
        this.userService = userService;
    }

    @Override
    public Integer call() {
        userService.registerUser(email,
                firstName,
                lastName,
                password,
                roles,
                UserStatus.ACTIVE,
                usageType,
                true);
        logger.info("User created.");
        return 0;
    }
}
