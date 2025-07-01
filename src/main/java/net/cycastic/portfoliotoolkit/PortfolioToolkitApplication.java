package net.cycastic.portfoliotoolkit;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.command.*;
import net.cycastic.portfoliotoolkit.configuration.HashicorpVaultConfiguration;
import net.cycastic.portfoliotoolkit.configuration.SymmetricEncryptionConfiguration;
import net.cycastic.portfoliotoolkit.configuration.auth.JwtConfiguration;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.DecryptionProvider;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import net.cycastic.portfoliotoolkit.service.impl.HashicorpVaultEncryptionProvider;
import net.cycastic.portfoliotoolkit.service.impl.SymmetricEncryptionProvider;
import net.cycastic.portfoliotoolkit.service.impl.auth.StandardJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.util.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@SpringBootApplication(exclude = FreeMarkerAutoConfiguration.class)
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class PortfolioToolkitApplication implements CommandLineRunner {
    private final CommandLine.IFactory picocliFactory;
    private final Cli cliCommand;

    @Component
    @CommandLine.Command(name = "tools",
            subcommands = {CreateUser.class, VerifyPassword.class, GenerateKeyPair.class, Cleanup.class,
                    CalculateAccumulatedStorage.class})
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
        var builder = new SpringApplicationBuilder(PortfolioToolkitApplication.class);
        if (args.length > 0){
            System.setProperty("spring.main.web-application-type", "none");
            builder = builder.web(WebApplicationType.NONE);
        }
        builder.run(args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            int exitCode = new CommandLine(cliCommand, picocliFactory)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .execute(args);
            System.exit(exitCode);
        }
    }
}
