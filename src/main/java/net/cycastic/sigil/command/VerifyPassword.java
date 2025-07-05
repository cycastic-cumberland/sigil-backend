package net.cycastic.sigil.command;

import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "verify-password", mixinStandardHelpOptions = true, description = "Create a new user")
public class VerifyPassword implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(VerifyPassword.class);
    @CommandLine.Option(names = "--email", required = true)
    private String email;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public VerifyPassword(UserRepository userRepository, PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Integer call() {
        var user = userRepository.getByEmail(email);
        if (user == null){
            logger.error("Could not find user with email {}", email);
            return 1;
        }
        if (!passwordHasher.verify(password, user.getPassword())){
            logger.error("Verification failed");
            return 1;
        }
        logger.info("Verification success");
        return 0;
    }
}
