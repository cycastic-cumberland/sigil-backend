package net.cycastic.sigil;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.command.*;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@SpringBootApplication(exclude = FreeMarkerAutoConfiguration.class)
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SigilApplication implements CommandLineRunner {
    private final CommandLine.IFactory picocliFactory;
    private final Cli cliCommand;

    @Component
    @CommandLine.Command(
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
        private final PasswordEncoder passwordEncoder;
        private final UserDetailsService userDetailsService;

        @Bean
        public AuthenticationProvider authenticationProvider(){
            var authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
        }
    }

    public static void main(String[] args) {
        var builder = new SpringApplicationBuilder(SigilApplication.class);
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
