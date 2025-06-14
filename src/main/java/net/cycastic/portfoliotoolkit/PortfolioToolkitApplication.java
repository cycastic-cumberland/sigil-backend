package net.cycastic.portfoliotoolkit;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.command.CreateUser;
import net.cycastic.portfoliotoolkit.command.GenerateKeyPair;
import net.cycastic.portfoliotoolkit.command.VerifyPassword;
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
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.util.Lazy;
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

    @Component
    @org.springframework.context.annotation.Lazy
    public static class EncryptionConfigurations{
        private final Lazy<HashicorpVaultEncryptionProvider> hashicorpVaultEncryptionProvider;
        private final Lazy<SymmetricEncryptionProvider> symmetricEncryptionProvider;
        private final Lazy<EncryptionProvider> encryptionProvider;
        private final Lazy<DecryptionProvider> decryptionProvider;
        private final Lazy<StandardJwtService> jwtService;

        @Autowired
        public EncryptionConfigurations(HashicorpVaultConfiguration hashicorpVaultConfiguration,
                                        SymmetricEncryptionConfiguration symmetricEncryptionConfiguration,
                                        JwtConfiguration jwtConfiguration){
            var vaultEnabled = false;
            Lazy<HashicorpVaultEncryptionProvider> hashicorpVaultEncryptionProvider = null;
            Lazy<SymmetricEncryptionProvider> symmetricEncryptionProvider = null;
            Lazy<EncryptionProvider> encryptionProvider = null;
            Lazy<DecryptionProvider> decryptionProvider = null;
            Lazy<StandardJwtService> jwtService = null;
            if (hashicorpVaultConfiguration.isValid()){
                vaultEnabled = true;
                hashicorpVaultEncryptionProvider = Lazy.of(() -> new HashicorpVaultEncryptionProvider(hashicorpVaultConfiguration));
                encryptionProvider = Lazy.of(hashicorpVaultEncryptionProvider);
                decryptionProvider = Lazy.of(hashicorpVaultEncryptionProvider);

                if (hashicorpVaultConfiguration.getSigningPrivateKeyWrapped() != null &&
                        hashicorpVaultConfiguration.getSigningPublicKey() != null){
                    jwtService = Lazy.of(() -> {
                        var privateKeyEncrypted = hashicorpVaultConfiguration.getSigningPrivateKeyWrapped();
                        var privateKeyBase64 = new HashicorpVaultEncryptionProvider(hashicorpVaultConfiguration).decrypt(privateKeyEncrypted);
                        var privateKey = StandardJwtService.decodePrivateKey(privateKeyBase64);
                        var publicKey = StandardJwtService.decodePublicKey(hashicorpVaultConfiguration.getSigningPublicKey());
                        return new StandardJwtService(jwtConfiguration, privateKey, publicKey);
                    });
                }
            }
            if (symmetricEncryptionConfiguration.isValid()){
                symmetricEncryptionProvider = Lazy.of(() -> new SymmetricEncryptionProvider(symmetricEncryptionConfiguration));
                if (!vaultEnabled){
                    encryptionProvider = Lazy.of(symmetricEncryptionProvider);
                    decryptionProvider = Lazy.of(symmetricEncryptionProvider);
                }
            }

            if (encryptionProvider == null || decryptionProvider == null){
                throw new IllegalStateException("No encryption setting configured");
            }

            this.encryptionProvider = encryptionProvider;
            this.decryptionProvider = decryptionProvider;
            this.hashicorpVaultEncryptionProvider = hashicorpVaultEncryptionProvider == null
                    ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                    : hashicorpVaultEncryptionProvider;

            this.symmetricEncryptionProvider = symmetricEncryptionProvider == null
                    ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                    : symmetricEncryptionProvider;

            this.jwtService = jwtService == null
                    ? Lazy.of(() -> new StandardJwtService(jwtConfiguration))
                    : jwtService;
        }

        @Bean
        public synchronized EncryptionProvider encryptionProvider(){
            return encryptionProvider.get();
        }

        @Bean
        public synchronized DecryptionProvider decryptionProvider(){
            return decryptionProvider.get();
        }

        @Bean
        public synchronized HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider(){
            return hashicorpVaultEncryptionProvider.get();
        }

        @Bean
        public synchronized SymmetricEncryptionProvider symmetricEncryptionProvider(){
            return symmetricEncryptionProvider.get();
        }

        @Bean
        public synchronized JwtIssuer jwtIssuer(){
            return jwtService.get();
        }

        @Bean
        public synchronized JwtVerifier jwtVerifier(){
            return jwtService.get();
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
