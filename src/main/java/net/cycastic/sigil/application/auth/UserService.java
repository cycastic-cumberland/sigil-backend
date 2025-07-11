package net.cycastic.sigil.application.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.configuration.RegistrationConfigurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.dto.CredentialDto;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.*;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.*;
import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import net.cycastic.sigil.service.impl.UriPresigner;
import net.cycastic.sigil.service.impl.auth.RSAKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String DUMMY_TEXT = "Hello World!";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final KeyDerivationFunction keyDerivationFunction;
    private final JwtIssuer jwtIssuer;
    private final String dummyHash;
    private final RegistrationConfigurations registrationConfigurations;
    private final UrlAccessor urlAccessor;
    private final UriPresigner uriPresigner;
    private final EmailTemplateEngine emailTemplateEngine;
    private final ApplicationEmailSender applicationEmailSender;
    private final TaskExecutor taskScheduler;
    private final CipherRepository cipherRepository;
    private final TenantService tenantService;

    @Autowired
    public UserService(LoggedUserAccessor loggedUserAccessor, UserRepository userRepository, PasswordValidator passwordValidator, KeyDerivationFunction keyDerivationFunction, JwtIssuer jwtIssuer, RegistrationConfigurations registrationConfigurations, UrlAccessor urlAccessor, UriPresigner uriPresigner, EmailTemplateEngine emailTemplateEngine, ApplicationEmailSender applicationEmailSender, TaskExecutor taskScheduler, CipherRepository cipherRepository, TenantService tenantService){
        this.loggedUserAccessor = loggedUserAccessor;
        this.userRepository = userRepository;
        this.passwordValidator = passwordValidator;
        this.keyDerivationFunction = keyDerivationFunction;
        this.jwtIssuer = jwtIssuer;
        this.dummyHash = keyDerivationFunction.encode(DUMMY_TEXT);
        this.registrationConfigurations = registrationConfigurations;
        this.urlAccessor = urlAccessor;
        this.uriPresigner = uriPresigner;
        this.emailTemplateEngine = emailTemplateEngine;
        this.applicationEmailSender = applicationEmailSender;
        this.taskScheduler = taskScheduler;
        this.cipherRepository = cipherRepository;
        this.tenantService = tenantService;
    }

    public static void refreshSecurityStamp(User user){
        CryptographicUtilities.generateRandom(user.getSecurityStamp());
    }

    @SneakyThrows
    private void enrollUserKeyPair(User user, KeyDerivationFunction.KeyDerivationResult keyEncryptionKey){
       var keyPair = RSAKeyGenerator.INSTANCE.generate();
        user.setPublicRsaKey(keyPair.getPublicKey().getEncoded());
        var hash = keyEncryptionKey.getHash();
        if (hash.length != CryptographicUtilities.KEY_LENGTH){
            throw new RequestException(400, "Invalid key encryption key");
        }
        var wrapKey = new SecretKeySpec(hash, "AES");
        var encodedPrivateKey = keyPair.getPrivateKey().getEncoded();
        var wrappedPrivateKey = CryptographicUtilities.encrypt(wrapKey, encodedPrivateKey);
        var kid = CryptographicUtilities.digestSha256(hash);
        var cipher = new Cipher(kid, CipherDecryptionMethod.USER_PASSWORD, wrappedPrivateKey);
        cipherRepository.save(cipher);
        user.setWrappedUserKey(cipher);
        user.setKdfSettings(keyEncryptionKey.getParameters().encode());
        user.setKdfSalt(keyEncryptionKey.getSalt());
    }

    private User registerUserNoTransactionNoValidation(@NotNull String email,
                                                       @NotNull String firstName,
                                                       @NotNull String lastName,
                                                       @NotNull String passwordKdf,
                                                       @NotNull KeyDerivationFunction.KeyDerivationResult keyEncryptionKey,
                                                       @NotNull Collection<String> roles,
                                                       @NotNull UserStatus userStatus,
                                                       @NotNull UsageType usageType,
                                                       boolean emailVerified){
        keyDerivationFunction.decode(passwordKdf); // Check validity
        var user = User.builder()
                .email(email)
                .normalizedEmail(email.toUpperCase(Locale.ROOT))
                .firstName(firstName)
                .lastName(lastName)
                .hashedPassword(passwordKdf)
                .roles(String.join(",", roles))
                .joinedAt(OffsetDateTime.now())
                .securityStamp(new byte[32])
                .status(userStatus)
                .lastInvitationSent(OffsetDateTime.now())
                .emailVerified(emailVerified)
                .build();
        enrollUserKeyPair(user, keyEncryptionKey);
        refreshSecurityStamp(user);
        userRepository.save(user);
        tenantService.createTenant(user, user.getLastName() + "'s tenant", usageType);
        return user;
    }

    public User registerUserNoTransaction(@NotNull String email,
                                          @NotNull String firstName,
                                          @NotNull String lastName,
                                          @NotNull String password,
                                          @NotNull Collection<String> roles,
                                          @NotNull UserStatus userStatus,
                                          @NotNull UsageType usageType,
                                          boolean emailVerified){
        if (!ApplicationUtilities.isEmail(email)){
            throw RequestException.withExceptionCode("C400T000");
        }
        passwordValidator.validate(password);
        var passwordKdf = keyDerivationFunction.encode(password);
        var salt = new byte[KeyDerivationFunction.SALT_SIZE];
        CryptographicUtilities.generateRandom(salt);
        var kekKdf = keyDerivationFunction.derive(password.getBytes(StandardCharsets.UTF_8), salt, keyDerivationFunction.getDefaultParameters());

        return registerUserNoTransactionNoValidation(email, firstName, lastName, passwordKdf, kekKdf, roles, userStatus, usageType, emailVerified);
    }

    @Transactional
    public User registerUser(@NotNull String email,
                             @NotNull String firstName,
                             @NotNull String lastName,
                             @NotNull String password,
                             @NotNull Collection<String> roles,
                             @NotNull UserStatus userStatus,
                             @NotNull UsageType usageType,
                             boolean emailVerified){
        return registerUserNoTransaction(email, firstName, lastName, password, roles, userStatus, usageType, emailVerified);
    }

    @SneakyThrows
    private String getKdfSettings(User user){
        final KeyDerivationFunction.Parameters parameters;
        try (var stream = new ByteArrayInputStream(user.getKdfSettings())){
            parameters = keyDerivationFunction.getParameters(stream);
        }
        final var salt = user.getKdfSalt();

        return keyDerivationFunction.encodeSettings(new KeyDerivationFunction.KeyDerivationSettings() {
            @Override
            public byte[] getSalt() {
                return salt;
            }

            @Override
            public KeyDerivationFunction.Parameters getParameters() {
                return parameters;
            }
        });
    }

    public CredentialDto createCredential(User user, String authToken){
        return CredentialDto.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .authToken(authToken)
                .publicRsaKey(Base64.getEncoder().encodeToString(user.getPublicRsaKey()))
                .kdfSettings(getKdfSettings(user))
                .wrappedUserKey(CipherDto.fromDomain(user.getWrappedUserKey()))
                .build();
    }

    private CredentialDto wasteComputePower(){
        keyDerivationFunction.matches(DUMMY_TEXT, dummyHash);
        jwtIssuer.generateTokens("", null);
        throw RequestException.withExceptionCode("C401T000");
    }

    private CredentialDto generateCredential(User user, String password){
        if (user == null || user.getPassword() == null){
            return wasteComputePower();
        }
        if (!keyDerivationFunction.matches(password, user.getPassword())){
            jwtIssuer.generateTokens("", null);
            throw RequestException.withExceptionCode("C401T000");
        }
        if (!user.isEmailVerified()){
            var now = OffsetDateTime.now();
            if (user.getLastInvitationSent() != null){
                var secondsElapsed = Duration.between(user.getLastInvitationSent(), now).getSeconds();
                if (secondsElapsed < registrationConfigurations.getResendVerificationLimitSeconds()){
                    throw RequestException.withExceptionCode("C401T001");
                }
            }

            user.setLastInvitationSent(now);
            userRepository.save(user);
            sendConfirmationEmail(user);
            throw new RequestException(400, "Verification email sent, please check your inbox");
        }
        if (!user.isEnabled()){
            return wasteComputePower();
        }


        var roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        var additionalClaims = new HashMap<String, Object>();
        additionalClaims.put(ApplicationConstants.ROLES_ENTRY, roles);
        additionalClaims.put(ApplicationConstants.SECURITY_STAMP_ENTRY, Base64.getEncoder().encodeToString(user.getSecurityStamp()));
        var token = jwtIssuer.generateTokens(user.getId().toString(), additionalClaims);
        return createCredential(user, token);
    }

    public CredentialDto generateCredential(String email, String password){
        if (registrationConfigurations.getResendVerificationLimitSeconds() <= 0){
            throw new IllegalStateException("Invalid resend verification time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }

        var user = userRepository.getByEmail(email);
        return generateCredential(user, password);
    }

    private URI generateCompletionUrl(UserDto user, String securityStamp, OffsetDateTime notValidBefore, OffsetDateTime notValidAfter){
        var backendCompletionUri = UriComponentsBuilder.fromUriString(urlAccessor.getBackendOrigin())
                .path("/api/auth/complete")
                .queryParam("userId", user.getId())
                .queryParam("securityStamp", ApplicationUtilities.encodeURIComponent(securityStamp))
                .queryParam("notValidBefore", notValidBefore.toInstant().getEpochSecond())
                .queryParam("notValidAfter", notValidAfter.toInstant().getEpochSecond())
                .build(true)
                .toUri();
        return uriPresigner.signUri(backendCompletionUri);
    }

    private static InputStream loadIconImage(){
        return UserService.class.getClassLoader().getResourceAsStream("static/logo.png");
    }

    @SneakyThrows
    private void sendConfirmationEmailInternal(@NotNull UserDto user, String securityStamp){
        var nvb = OffsetDateTime.now();
        var nva = nvb.plusSeconds(registrationConfigurations.getRegistrationLinkValidSeconds());
        var backendCompletionUri = generateCompletionUrl(user, securityStamp, nvb, nva);
        var completionUri = UriComponentsBuilder.fromUriString(urlAccessor.getFrontendOrigin())
                .path("/complete-signup")
                .queryParam("submission", ApplicationUtilities.encodeURIComponent(backendCompletionUri))
                .build(true)
                .toUri();
        Map<String, Object> parameters = Map.of(
                "completionUri", completionUri,
                "__debugBackendCompletionUri", backendCompletionUri,
                "lastName", user.getLastName(),
                "notValidAfter", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.ROOT)
                        .format(nva),
                "logo", new EmailImage() {
                    @Override
                    public String getFileName() {
                        return "logo.png";
                    }

                    @Override
                    public String getMimeType() {
                        return "image/png";
                    }

                    @Override
                    public InputStreamSource getImageSource() {
                        return UserService::loadIconImage;
                    }
                }
        );
        byte[] renderedContent;
        EmailTemplateEngine.RenderResult renderResult;

        try (var byteStream = new ByteArrayOutputStream()){
            try (var templateStream = getClass().getClassLoader().getResourceAsStream("templates/register/RegistrationCompletionMail.ftl")){
                renderResult = emailTemplateEngine.render(templateStream, byteStream, parameters);
            }

            renderedContent = byteStream.toByteArray();
        }

        applicationEmailSender.sendHtml(user.getEmail(),
                null,
                "Complete your registration with PortfolioToolkit",
                new String(renderedContent, StandardCharsets.UTF_8),
                renderResult.getImageStreamSource());
    }

    public void sendConfirmationEmail(User user){
        if (registrationConfigurations.getRegistrationLinkValidSeconds() <= 0){
            throw new IllegalStateException("Invalid registration expiration time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }
        if (registrationConfigurations.getResendVerificationLimitSeconds() <= 0){
            throw new IllegalStateException("Invalid resend verification time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }
        final var securityStamp = Base64.getEncoder().encodeToString(user.getSecurityStamp());
        final var dto = UserDto.fromDomain(user);

        taskScheduler.execute(() -> {
            try {
                sendConfirmationEmailInternal(dto, securityStamp);
                logger.info("Confirmation email sent to user {}", dto.getId());
            } catch (Exception e) {
                logger.error("Failed to send confirmation email to user {}", dto.getId(), e);
            }
        });
    }

    public User getUser(){
        return userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
    }

    @SneakyThrows
    public PublicKey getUserPublicKey(){
        var keyEncoded = getUser().getPublicRsaKey();
        var kf = KeyFactory.getInstance("RSA", "BC");
        return kf.generatePublic(new X509EncodedKeySpec(keyEncoded));
    }
}
