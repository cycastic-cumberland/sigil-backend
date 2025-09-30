package net.cycastic.sigil.application.user;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.sigil.configuration.RegistrationConfigurations;
import net.cycastic.sigil.configuration.auth.KdfConfiguration;
import net.cycastic.sigil.domain.ApplicationAssets;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.*;
import net.cycastic.sigil.domain.model.notification.NotificationToken;
import net.cycastic.sigil.domain.model.notification.NotificationTokenConsumer;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.WebAuthnCredentialRepository;
import net.cycastic.sigil.domain.repository.notifications.NotificationTokenRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.*;
import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import net.cycastic.sigil.service.email.DeferredEmailSender;
import net.cycastic.sigil.service.email.EmailTemplateEngine;
import net.cycastic.sigil.service.email.EmailTemplates;
import net.cycastic.sigil.service.impl.UriPresigner;
import net.cycastic.sigil.service.job.JobScheduler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    public static long SIGNATURE_VERIFICATION_WINDOW = 10;

    private final LoggedUserAccessor loggedUserAccessor;
    private final WebAuthnCredentialRepository webAuthnCredentialRepository;
    private final UserRepository userRepository;
    private final KeyDerivationFunction keyDerivationFunction;
    private final JwtIssuer jwtIssuer;
    private final RegistrationConfigurations registrationConfigurations;
    private final UrlAccessor urlAccessor;
    private final UriPresigner uriPresigner;
    private final EmailTemplateEngine emailTemplateEngine;
    private final DeferredEmailSender deferredEmailSender;
    private final JobScheduler jobScheduler;
    private final CipherRepository cipherRepository;
    private final KdfConfiguration kdfConfiguration;
    private final TenantRepository tenantRepository;
    private final NotificationTokenRepository notificationTokenRepository;

    public UserService(LoggedUserAccessor loggedUserAccessor, WebAuthnCredentialRepository webAuthnCredentialRepository,
                       UserRepository userRepository,
                       KeyDerivationFunction keyDerivationFunction,
                       JwtIssuer jwtIssuer,
                       RegistrationConfigurations registrationConfigurations,
                       UrlAccessor urlAccessor,
                       UriPresigner uriPresigner,
                       EmailTemplateEngine emailTemplateEngine,
                       DeferredEmailSender deferredEmailSender,
                       JobScheduler jobScheduler,
                       CipherRepository cipherRepository,
                       KdfConfiguration kdfConfiguration, TenantRepository tenantRepository, NotificationTokenRepository notificationTokenRepository){
        if (registrationConfigurations.getRegistrationLinkValidSeconds() <= 0){
            throw new IllegalStateException("Invalid registration expiration time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }
        if (registrationConfigurations.getResendVerificationLimitSeconds() <= 0){
            throw new IllegalStateException("Invalid resend verification time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }

        this.loggedUserAccessor = loggedUserAccessor;
        this.webAuthnCredentialRepository = webAuthnCredentialRepository;
        this.userRepository = userRepository;
        this.keyDerivationFunction = keyDerivationFunction;
        this.jwtIssuer = jwtIssuer;
        this.kdfConfiguration = kdfConfiguration;
        this.registrationConfigurations = registrationConfigurations;
        this.urlAccessor = urlAccessor;
        this.uriPresigner = uriPresigner;
        this.emailTemplateEngine = emailTemplateEngine;
        this.deferredEmailSender = deferredEmailSender;
        this.jobScheduler = jobScheduler;
        this.cipherRepository = cipherRepository;
        this.tenantRepository = tenantRepository;
        this.notificationTokenRepository = notificationTokenRepository;
    }

    public static void refreshSecurityStamp(User user){
        CryptographicUtilities.generateRandom(user.getSecurityStamp());
    }

    private User registerUserNoTransactionNoValidation(@NotNull String email,
                                                       @NotNull Collection<String> roles,
                                                       @NotNull UserStatus userStatus,
                                                       boolean emailVerified){
        email = email.trim();
        var notificationToken = NotificationToken.builder()
                .consumer(NotificationTokenConsumer.USER)
                .build();
        var user = User.builder()
                .email(email)
                .normalizedEmail(email.toUpperCase(Locale.ROOT))
                .roles(String.join(",", roles))
                .joinedAt(OffsetDateTime.now())
                .securityStamp(new byte[32])
                .status(userStatus)
                .lastInvitationSent(null)
                .emailVerified(emailVerified)
                .notificationToken(notificationToken)
                .build();
        refreshSecurityStamp(user);
        notificationTokenRepository.save(notificationToken);
        userRepository.save(user);
        return user;
    }

    public User registerUserNoTransaction(@NotNull String email,
                                          @NotNull Collection<String> roles,
                                          @NotNull UserStatus userStatus,
                                          boolean emailVerified){
        if (!ApplicationUtilities.isEmail(email)){
            throw RequestException.withExceptionCode("C400T000");
        }
        return registerUserNoTransactionNoValidation(email, roles, userStatus, emailVerified);
    }

    @SneakyThrows
    @Deprecated
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
        throw new NotSupportedException("This method is no longer supported");
    }

    @SneakyThrows
    private String getKdfSettings(User user){
        if (user.getKdfSettings() == null || user.getKdfSalt() == null){
            return null;
        }
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
                .notificationToken(user.getNotificationToken().getToken())
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
        var publicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(Base64.getDecoder().decode(kdfConfiguration.getMaskingRsaPublicKey()));
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
        var publicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(user.getPublicRsaKey());
        if (user.getStatus() != UserStatus.ACTIVE ||
                !checkSignInPayloadValidity(payload, signatureAlgorithm, submittedSignature, publicKey)){
            jwtIssuer.generateTokens("", null);
            throw RequestException.withExceptionCode("C401T000");
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

    private URI generateCompletionUri(UserDto user, String securityStamp, OffsetDateTime notValidBefore, OffsetDateTime notValidAfter){
        var backendCompletionUri = UriComponentsBuilder.fromUriString(urlAccessor.getBackendOrigin())
                .path("/api/auth/register/complete")
                .queryParam("userId", user.getId())
                .queryParam("securityStamp", ApplicationUtilities.encodeURIComponent(securityStamp))
                .queryParam("notValidBefore", notValidBefore.toInstant().getEpochSecond())
                .queryParam("notValidAfter", notValidAfter.toInstant().getEpochSecond())
                .build(true)
                .toUri();
        return uriPresigner.signUri(backendCompletionUri);
    }

    @SneakyThrows
    public void sendConfirmationEmailInternal(@NotNull UserDto user, String securityStamp){
        var nvb = OffsetDateTime.now();
        var nva = nvb.plusSeconds(registrationConfigurations.getRegistrationLinkValidSeconds());
        var backendCompletionUri = generateCompletionUri(user, securityStamp, nvb, nva);
        var completionUri = UriComponentsBuilder.fromUriString(urlAccessor.getFrontendOrigin())
                .path("/complete-signup")
                .queryParam("submission", ApplicationUtilities.encodeURIComponent(backendCompletionUri))
                .build(true)
                .toUri();
        Map<String, Object> parameters = Map.of(
                "completionUri", completionUri,
                "__debugBackendCompletionUri", backendCompletionUri,
                "notValidAfter", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.ROOT)
                        .format(nva),
                "logo", ApplicationAssets.EmailImages.LOGO_EMAIL_IMAGE
        );
        byte[] renderedContent;
        EmailTemplateEngine.RenderResult renderResult;

        try (var byteStream = new ByteArrayOutputStream()){
            try (var templateStream = EmailTemplates.registrationCompletion()){
                renderResult = emailTemplateEngine.render(templateStream, byteStream, parameters);
            }

            renderedContent = byteStream.toByteArray();
        }

        deferredEmailSender.sendHtml(user.getEmail(),
                null,
                "Complete your registration with Sigil",
                new String(renderedContent, StandardCharsets.UTF_8),
                renderResult.getImageStreamSource());
    }

    public void sendConfirmationEmailNoTransaction(User user){
        var now = OffsetDateTime.now();
        if (user.getLastInvitationSent() != null){
            var secondsElapsed = Duration.between(user.getLastInvitationSent(), now).getSeconds();
            if (secondsElapsed < registrationConfigurations.getResendVerificationLimitSeconds()){
                throw RequestException.withExceptionCode("C400T004", registrationConfigurations.getResendVerificationLimitSeconds() - secondsElapsed);
            }
        }

        user.setLastInvitationSent(now);
        userRepository.save(user);
        final var securityStamp = Base64.getEncoder().encodeToString(user.getSecurityStamp());
        final var dto = UserDto.fromDomain(user);

        jobScheduler.defer(SendConfirmationEmailJob.builder()
                        .user(dto)
                        .securityStamp(securityStamp)
                .build());
    }

    public void enrollWebAuthnNoTransaction(User user, WebAuthnCredentialDto webAuthnCredential){
        if (webAuthnCredential.getWrappedUserKey().getDecryptionMethod() != CipherDecryptionMethod.WEBAUTHN_KEY){
            throw RequestException.withExceptionCode("C400T012");
        }
        if (webAuthnCredentialRepository.existsByUser(user)){
            throw RequestException.withExceptionCode("C409T000");
        }
        var transports = Arrays.stream(webAuthnCredential.getTransports())
                .map(String::trim)
                .map(String::toLowerCase)
                .sorted(String::compareTo)
                .toList();
        if (transports.stream().anyMatch(t -> t.contains(","))){
            throw new RequestException(400, "Invalid transport");
        }

        var cipher = new Cipher(CipherDecryptionMethod.WEBAUTHN_KEY,
                Base64.getDecoder().decode(webAuthnCredential.getWrappedUserKey().getIv()),
                Base64.getDecoder().decode(webAuthnCredential.getWrappedUserKey().getCipher()));
        cipherRepository.save(cipher);

        var existingCred = user.getWebAuthnCredential();
        if (existingCred != null){
            webAuthnCredentialRepository.delete(existingCred);
            user.setWebAuthnCredential(null);
        }
        var cred = WebAuthnCredential.builder()
                .user(user)
                .credentialId(Base64.getDecoder().decode(webAuthnCredential.getCredentialId()))
                .salt(Base64.getDecoder().decode(webAuthnCredential.getSalt()))
                .transports(String.join(",", transports))
                .wrappedUserKey(cipher)
                .build();
        webAuthnCredentialRepository.save(cred);
    }

    public void completeRegistrationNoTransaction(User user, CompleteUserRegistrationForm form){
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPublicRsaKey(Base64.getDecoder().decode(form.getPublicRsaKey()));
        if (form.getPasswordCredential() == null && form.getWebAuthnCredential() == null){
            throw RequestException.withExceptionCode("C400T010");
        }
        if (form.getPasswordCredential() != null){
            if (form.getPasswordCredential().getCipher().getDecryptionMethod() != CipherDecryptionMethod.USER_PASSWORD){
                throw RequestException.withExceptionCode("C400T012");
            }
            var settings = keyDerivationFunction.decodeSettings(form.getPasswordCredential().getKeyDerivationSettings());
            if (!settings.getParameters().isMinimallyViable()){
                throw new RequestException(400, "Password cipher does not achieve minimum viability");
            }
            user.setKdfSalt(settings.getSalt());
            user.setKdfSettings(settings.getParameters().encode());
            var passwordCipher = new Cipher(CipherDecryptionMethod.USER_PASSWORD,
                    Base64.getDecoder().decode(form.getPasswordCredential().getCipher().getIv()),
                    Base64.getDecoder().decode(form.getPasswordCredential().getCipher().getCipher()));
            cipherRepository.save(passwordCipher);
            user.setWrappedUserKey(passwordCipher);
        } else {
            enrollWebAuthnNoTransaction(user, form.getWebAuthnCredential());
        }

        user.setUpdatedAt(OffsetDateTime.now());
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public User getUser(){
        return userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
    }

    public UserDto getUserDetails(User user){
        var dto = UserDto.fromDomain(user);
        var count = tenantRepository.countByOwner_Id(user.getId());
        dto.setTenantOwnerCount(count);
        return dto;
    }

    @Transactional
    public void updatePasswordCipher(int userId, PrivateKey privateKey, String newPassword){
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var salt = new byte[KeyDerivationFunction.SALT_SIZE];
        CryptographicUtilities.generateRandom(salt);
        var keyDerivationResult = keyDerivationFunction.derive(newPassword.getBytes(StandardCharsets.UTF_8), salt, keyDerivationFunction.getDefaultParameters());
        user.setKdfSalt(salt);
        user.setKdfSettings(keyDerivationResult.getParameters().encode());

        var iv = new byte[CryptographicUtilities.NONCE_LENGTH];
        CryptographicUtilities.generateRandom(iv);
        var dummyKey = new SecretKeySpec(keyDerivationResult.getHash(), "AES");
        var encryptedPrivateKey = CryptographicUtilities.encrypt(dummyKey, iv, privateKey.getEncoded());
        var cipher = new Cipher(CipherDecryptionMethod.USER_PASSWORD, iv, encryptedPrivateKey);

        if (user.getWrappedUserKey() != null){
            cipherRepository.delete(user.getWrappedUserKey());
        }

        cipherRepository.save(cipher);
        user.setWrappedUserKey(cipher);
        userRepository.save(user);
    }
}
