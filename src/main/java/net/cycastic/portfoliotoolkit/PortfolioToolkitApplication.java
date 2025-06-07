package net.cycastic.portfoliotoolkit;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.command.CreateUser;
import net.cycastic.portfoliotoolkit.command.GenerateKeyPair;
import net.cycastic.portfoliotoolkit.command.VerifyPassword;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@SpringBootApplication
@RequiredArgsConstructor
public class PortfolioToolkitApplication implements CommandLineRunner {
    private final CommandLine.IFactory picocliFactory;
    private final Cli cliCommand;

    @Component
    @CommandLine.Command(name = "tools", subcommands = {CreateUser.class, VerifyPassword.class, GenerateKeyPair.class})
    public static class Cli{ }

    @Component
    @RequiredArgsConstructor
    public static class UserDetailsConfigurations {
        private final UserRepository userRepository;

        @Bean
        public UserDetailsService userDetailsService(){
            return username -> {
                var id = ApplicationUtilities.tryParseInt(username)
                        .orElseThrow(() -> new RequestException(404, "Could not find user"));
                return userRepository.findById(id)
                        .orElseThrow(() -> new RequestException(404, "Could not find user"));
            };
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class AuthenticationProviderConfigurations{
        private final PasswordHasher passwordHasher;
        private final UserDetailsService userDetailsService;

        @Bean
        public AuthenticationProvider authenticationProvider(){
            var authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordHasher);
            return authProvider;
        }
    }

    public static void main(String[] args) {
        if (args.length > 0){
            System.setProperty("spring.main.web-application-type", "none");
        }
        SpringApplication.run(PortfolioToolkitApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            int exitCode = new CommandLine(cliCommand, picocliFactory)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .execute(args);
            System.exit(exitCode); // Exit JVM after command execution
        }
    }
}
