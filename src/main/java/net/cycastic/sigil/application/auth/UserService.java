package net.cycastic.sigil.application.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.configuration.RegistrationConfigurations;
import net.cycastic.sigil.configuration.auth.KdfConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    public static long SIGNATURE_VERIFICATION_WINDOW = 3;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final KeyDerivationFunction keyDerivationFunction;
    private final JwtIssuer jwtIssuer;
    private final RegistrationConfigurations registrationConfigurations;
    private final UrlAccessor urlAccessor;
    private final UriPresigner uriPresigner;
    private final EmailTemplateEngine emailTemplateEngine;
    private final ApplicationEmailSender applicationEmailSender;
    private final TaskExecutor taskScheduler;
    private final CipherRepository cipherRepository;
    private final TenantService tenantService;
    private final KdfConfiguration kdfConfiguration;

    @Autowired
    public UserService(LoggedUserAccessor loggedUserAccessor, UserRepository userRepository, KeyDerivationFunction keyDerivationFunction, JwtIssuer jwtIssuer, RegistrationConfigurations registrationConfigurations, UrlAccessor urlAccessor, UriPresigner uriPresigner, EmailTemplateEngine emailTemplateEngine, ApplicationEmailSender applicationEmailSender, TaskExecutor taskScheduler, CipherRepository cipherRepository, TenantService tenantService, KdfConfiguration kdfConfiguration){
        this.loggedUserAccessor = loggedUserAccessor;
        this.userRepository = userRepository;
        this.keyDerivationFunction = keyDerivationFunction;
        this.jwtIssuer = jwtIssuer;
        this.kdfConfiguration = kdfConfiguration;
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

    private User registerUserNoTransactionNoValidation(@NotNull String email,
                                                       @NotNull String firstName,
                                                       @NotNull String lastName,
                                                       @NotNull String publicRsaKey,
                                                       @NotNull String kdfSalt,
                                                       @NotNull String kdfParameters,
                                                       @NotNull CipherDto privateRsaKey,
                                                       @NotNull Collection<String> roles,
                                                       @NotNull UserStatus userStatus,
                                                       @NotNull UsageType usageType,
                                                       boolean emailVerified){
        var decoder = Base64.getDecoder();
        var user = User.builder()
                .email(email)
                .normalizedEmail(email.toUpperCase(Locale.ROOT))
                .firstName(firstName)
                .lastName(lastName)
                .publicRsaKey(decoder.decode(publicRsaKey))
                .roles(String.join(",", roles))
                .joinedAt(OffsetDateTime.now())
                .securityStamp(new byte[32])
                .status(userStatus)
                .lastInvitationSent(OffsetDateTime.now())
                .emailVerified(emailVerified)
                .build();
        var cipher = new Cipher(decoder.decode(privateRsaKey.getKid()),
                CipherDecryptionMethod.USER_PASSWORD,
                decoder.decode(privateRsaKey.getIv()),
                decoder.decode(privateRsaKey.getCipher()));
        cipherRepository.save(cipher);
        user.setWrappedUserKey(cipher);
        user.setKdfSettings(decoder.decode(kdfParameters));
        user.setKdfSalt(decoder.decode(kdfSalt));
        refreshSecurityStamp(user);
        userRepository.save(user);
        tenantService.createTenant(user, user.getLastName() + "'s tenant", usageType);
        return user;
    }

    public User registerUserNoTransaction(@NotNull String email,
                                          @NotNull String firstName,
                                          @NotNull String lastName,
                                          @NotNull String publicRsaKey,
                                          @NotNull String kdfSalt,
                                          @NotNull String kdfParameters,
                                          @NotNull CipherDto privateRsaKey,
                                          @NotNull Collection<String> roles,
                                          @NotNull UserStatus userStatus,
                                          @NotNull UsageType usageType,
                                          boolean emailVerified){
        if (!ApplicationUtilities.isEmail(email)){
            throw RequestException.withExceptionCode("C400T000");
        }
        return registerUserNoTransactionNoValidation(email, firstName, lastName, publicRsaKey, kdfSalt, kdfParameters, privateRsaKey, roles, userStatus, usageType, emailVerified);
    }

    @Transactional
    public User registerUser(@NotNull String email,
                             @NotNull String firstName,
                             @NotNull String lastName,
                             @NotNull String publicRsaKey,
                             @NotNull String kdfSalt,
                             @NotNull String kdfParameters,
                             @NotNull CipherDto privateRsaKey,
                             @NotNull Collection<String> roles,
                             @NotNull UserStatus userStatus,
                             @NotNull UsageType usageType,
                             boolean emailVerified){
        return registerUserNoTransaction(email, firstName, lastName, publicRsaKey, kdfSalt, kdfParameters, privateRsaKey, roles, userStatus, usageType, emailVerified);
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

    private static boolean checkSignInPayloadValidity(String payload, String signatureAlgorithm, byte[] submittedSignature, PublicKey publicKey){
        var result = true;
        result &= CryptographicUtilities.verifySignature(payload.getBytes(StandardCharsets.UTF_8), submittedSignature, signatureAlgorithm, publicKey);
        var slices = payload.split(":");
        if (slices.length != 2){
            throw new RequestException(400, "Invalid payload");
        }

        var requestTimeStep = ApplicationUtilities.tryParseLong(slices[1])
                .orElseThrow(() -> new RequestException(400, "Invalid payload"));
        var currentTimeStep = CryptographicUtilities.TOTP.getTimeStamp(Instant.now().getEpochSecond(), SIGNATURE_VERIFICATION_WINDOW);
        var correctTimeStep = false;
        correctTimeStep |= requestTimeStep == currentTimeStep;
        correctTimeStep |= (requestTimeStep - 1) == currentTimeStep;
        result &= correctTimeStep;
        return result;
    }

    @SneakyThrows
    private CredentialDto wasteComputePower(String payload, String signatureAlgorithm, byte[] submittedSignature){
        var kf = KeyFactory.getInstance("RSA", "BC");
        var publicKey = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(kdfConfiguration.getMaskingRsaPublicKey())));
        var result = checkSignInPayloadValidity(payload, signatureAlgorithm, submittedSignature, publicKey);
        ApplicationUtilities.deoptimize(result);

        jwtIssuer.generateTokens("", null);
        throw RequestException.withExceptionCode("C401T000");
    }

    @SneakyThrows
    private CredentialDto generateCredential(Optional<User> userOpt, String payload, String signatureAlgorithm, byte[] submittedSignature){
        if (userOpt.isEmpty()){
            return wasteComputePower("email@example.com:1", signatureAlgorithm, submittedSignature);
        }

        var user = userOpt.get();
        if (!user.isEnabled()){
            return wasteComputePower(user.getNormalizedEmail(), signatureAlgorithm, submittedSignature);
        }
        var kf = KeyFactory.getInstance("RSA", "BC");
        var publicKey = kf.generatePublic(new X509EncodedKeySpec(user.getPublicRsaKey()));
        if (!checkSignInPayloadValidity(payload, signatureAlgorithm, submittedSignature, publicKey)){
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

    public CredentialDto generateCredential(String payload, String signatureAlgorithm, byte[] submittedSignature){
        if (registrationConfigurations.getResendVerificationLimitSeconds() <= 0){
            throw new IllegalStateException("Invalid resend verification time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }

        var slices = payload.split(":");
        if (slices.length != 2){
            throw new RequestException(400, "Invalid payload");
        }
        if (ApplicationUtilities.tryParseLong(slices[1]).isEmpty()){
            throw new RequestException(400, "Invalid payload");
        }
        var user = userRepository.getByEmail(slices[0]);
        return generateCredential(user, payload, signatureAlgorithm, submittedSignature);
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
}
