package net.cycastic.sigil.application.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.RegistrationConfigurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.dto.CredentialDto;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.*;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.*;
import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String DUMMY_TEXT = "Hello World!";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;
    private final JwtIssuer jwtIssuer;
    private final String dummyHash;
    private final RegistrationConfigurations registrationConfigurations;
    private final UrlAccessor urlAccessor;
    private final UriPresigner uriPresigner;
    private final EmailTemplateEngine emailTemplateEngine;
    private final ApplicationEmailSender applicationEmailSender;
    private final TaskExecutor taskScheduler;
    private final CipherRepository cipherRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordHasher passwordHasher, PasswordValidator passwordValidator, JwtIssuer jwtIssuer, RegistrationConfigurations registrationConfigurations, UrlAccessor urlAccessor, UriPresigner uriPresigner, EmailTemplateEngine emailTemplateEngine, ApplicationEmailSender applicationEmailSender, TaskExecutor taskScheduler, CipherRepository cipherRepository){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
        this.jwtIssuer = jwtIssuer;
        this.dummyHash = passwordHasher.hash(DUMMY_TEXT);
        this.registrationConfigurations = registrationConfigurations;
        this.urlAccessor = urlAccessor;
        this.uriPresigner = uriPresigner;
        this.emailTemplateEngine = emailTemplateEngine;
        this.applicationEmailSender = applicationEmailSender;
        this.taskScheduler = taskScheduler;
        this.cipherRepository = cipherRepository;
    }

    public static void refreshSecurityStamp(User user){
        RANDOM.nextBytes(user.getSecurityStamp());
    }

    @SneakyThrows
    private void reEnrollKeyPair(User.UserBuilder user, byte[] key){
        var keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(2048);
        var keyPair = keyGen.generateKeyPair();
        user.publicRsaKey(keyPair.getPublic().getEncoded());
        var wrapKey = new SecretKeySpec(CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH,
                key,
                null),
                "AES");
        var encodedPrivateKey = keyPair.getPrivate().getEncoded();
        var wrappedPrivateKey = CryptographicUtilities.encrypt(wrapKey, encodedPrivateKey);
        var kid = CryptographicUtilities.digestSha256(encodedPrivateKey);
        var cipher = new Cipher(null, kid, CipherEncryptionMethod.USER_PASSWORD, wrappedPrivateKey.getIv(), null, null);
        cipher.setCipher(wrappedPrivateKey.getCipher());
        cipherRepository.save(cipher);
        user.wrappedUserKey(cipher);
    }

    public User registerUserNoTransaction(@NotNull String email,
                                          @NotNull String firstName,
                                          @NotNull String lastName,
                                          @NotNull String password,
                                          @NotNull Collection<String> roles,
                                          @NotNull UserStatus userStatus,
                                          boolean emailVerified){
        if (!ApplicationUtilities.isEmail(email)){
            throw new RequestException(400, "Malformed email address");
        }
        passwordValidator.validate(password);

        var userBuilder = User.builder()
                .email(email)
                .normalizedEmail(email.toUpperCase(Locale.ROOT))
                .firstName(firstName)
                .lastName(lastName)
                .hashedPassword(passwordHasher.hash(password))
                .roles(String.join(",", roles))
                .joinedAt(OffsetDateTime.now())
                .securityStamp(new byte[32])
                .status(userStatus)
                .lastInvitationSent(OffsetDateTime.now())
                .emailVerified(emailVerified);
        reEnrollKeyPair(userBuilder, password.getBytes(StandardCharsets.UTF_8));
        var user = userBuilder.build();
        refreshSecurityStamp(user);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User registerUser(@NotNull String email,
                             @NotNull String firstName,
                             @NotNull String lastName,
                             @NotNull String password,
                             @NotNull Collection<String> roles,
                             @NotNull UserStatus userStatus,
                             boolean emailVerified){
        return registerUserNoTransaction(email, firstName, lastName, password, roles, userStatus, emailVerified);
    }

    private CredentialDto wasteComputePower(){
        passwordHasher.verify(DUMMY_TEXT, dummyHash);
        jwtIssuer.generateTokens("", null);
        throw new RequestException(401, "Incorrect credential");
    }

    private CredentialDto generateCredential(User user, String password){
        if (user == null || user.getPassword() == null){
            return wasteComputePower();
        }
        if (!passwordHasher.verify(password, user.getPassword())){
            jwtIssuer.generateTokens("", null);
            throw new RequestException(401, "Incorrect credential");
        }
        if (!user.isEmailVerified()){
            var now = OffsetDateTime.now();
            if (user.getLastInvitationSent() != null){
                var secondsElapsed = Duration.between(user.getLastInvitationSent(), now).getSeconds();
                if (secondsElapsed < registrationConfigurations.getResendVerificationLimitSeconds()){
                    throw new RequestException(401, "Email not verified, please restart registration");
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
        return CredentialDto.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .authToken(token)
                .publicRsaKey(Base64.getEncoder().encodeToString(user.getPublicRsaKey()))
                .wrappedUserKey(CipherDto.fromDomain(user.getWrappedUserKey()))
                .build();
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
                .build()
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
}
