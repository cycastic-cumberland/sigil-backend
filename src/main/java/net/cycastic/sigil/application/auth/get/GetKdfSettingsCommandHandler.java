package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.configuration.auth.KdfConfiguration;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Component
public class GetKdfSettingsCommandHandler implements Command.Handler<GetKdfSettingsCommand, KdfDetailsDto> {
    private static final int STANDARD_WEBAUTHN_ID_LENGTH = 20;
    private static final int STANDARD_WEBAUTHN_SALT_LENGTH = 32;

    @RequiredArgsConstructor
    private static class KdfSettings implements KeyDerivationFunction.KeyDerivationSettings{
        private final KeyDerivationFunction kdf;
        private final byte[] salt;
        private final byte[] parameters;

        @Override
        public byte[] getSalt() {
            return salt;
        }

        @Override
        @SneakyThrows
        public KeyDerivationFunction.Parameters getParameters() {
            try (var stream = new ByteArrayInputStream(parameters)){
                return kdf.getParameters(stream);
            }
        }
    }

    private final UserRepository userRepository;
    private final KeyDerivationFunction keyDerivationFunction;
    private final KeyDerivationFunction.Parameters defaultParameters;
    private final byte[] maskingKey;
    private final byte[] maskingPrivateKey;

    @Autowired
    public GetKdfSettingsCommandHandler(UserRepository userRepository,
                                        KeyDerivationFunction keyDerivationFunction,
                                        KdfConfiguration kdfConfiguration) {
        this.userRepository = userRepository;
        this.keyDerivationFunction = keyDerivationFunction;

        defaultParameters = keyDerivationFunction.getDefaultParameters();
        byte[] salt;
        if (kdfConfiguration.getMaskingKey() != null){
            try {
                salt = Base64.getDecoder().decode(kdfConfiguration.getMaskingKey());
            } catch (IllegalArgumentException e){
                salt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, kdfConfiguration.getMaskingKey().getBytes(StandardCharsets.UTF_8), null);
            }
        } else {
            salt = new byte[KeyDerivationFunction.SALT_SIZE];
        }

        maskingKey = salt;
        maskingPrivateKey = Base64.getDecoder().decode(kdfConfiguration.getMaskingRsaPrivateKey());
    }

    private byte[] getMaskingSeed(String... seeds){
        assert seeds.length > 0;
        final var seedLength = 64;
        var seed = CryptographicUtilities.deriveKey(seedLength, maskingKey, seeds[0].getBytes(StandardCharsets.UTF_8));
        for (var i = 1; i < seeds.length; i++){
            seed = CryptographicUtilities.deriveKey(seedLength, seed, seeds[i].getBytes(StandardCharsets.UTF_8));
        }

        return seed;
    }

    private KeyDerivationFunction.KeyDerivationSettings createDummyDetails(String... seeds){
        var finalSeed = getMaskingSeed(seeds);
        var dummySalt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, finalSeed, "kdf-salt".getBytes(StandardCharsets.UTF_8));

        return new KeyDerivationFunction.KeyDerivationSettings() {
            @Override
            public byte[] getSalt() {
                return dummySalt;
            }

            @Override
            public KeyDerivationFunction.Parameters getParameters() {
                return defaultParameters;
            }
        };
    }

    private CipherDto createDummyCipher(CipherDecryptionMethod method, String... seeds){
        var finalSeed = getMaskingSeed(seeds);

        var dummyEncryptionKey = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, finalSeed, "kek".getBytes(StandardCharsets.UTF_8));
        var dummyEncryptionNonce = CryptographicUtilities.deriveKey(CryptographicUtilities.NONCE_LENGTH, finalSeed, "nonce".getBytes(StandardCharsets.UTF_8));
        var dummyKey = new SecretKeySpec(dummyEncryptionKey, "AES");
        var dummyEncryptedPrivateKey = CryptographicUtilities.encrypt(dummyKey, dummyEncryptionNonce, maskingPrivateKey);

        return CipherDto.builder()
                .decryptionMethod(method)
                .iv(Base64.getEncoder().encodeToString(dummyEncryptionNonce))
                .cipher(Base64.getEncoder().encodeToString(dummyEncryptedPrivateKey))
                .build();
    }

    @Override
    @SneakyThrows
    public KdfDetailsDto handle(GetKdfSettingsCommand command) {
        var method = Objects.requireNonNullElse(command.getMethod(), AuthenticationMethod.PASSWORD);
        var dummySettings = createDummyDetails(command.getUserEmail());
        var dummyPasswordCipher = createDummyCipher(CipherDecryptionMethod.USER_PASSWORD, command.getUserEmail(), "PasswordCipher");
        var dummyWebAuthnCipher = createDummyCipher(CipherDecryptionMethod.WEBAUTHN_KEY, command.getUserEmail(), "WebAuthnCipher");
        var dummyWebAuthnId = CryptographicUtilities.deriveKey(STANDARD_WEBAUTHN_ID_LENGTH, getMaskingSeed(command.getUserEmail(), "WebAuthnKeyId"), null);
        var dummyWebAuthnSalt = CryptographicUtilities.deriveKey(STANDARD_WEBAUTHN_SALT_LENGTH, getMaskingSeed(command.getUserEmail(), "WebAuthnKeySalt"), null);

        var userOpt = userRepository.getByEmail(command.getUserEmail());
        if (userOpt.isPresent() && userOpt.get().getStatus() != UserStatus.ACTIVE){
            userOpt = Optional.empty();
        }

        var passwordWrappedKey = dummyPasswordCipher;
        var webAuthnCredential = WebAuthnCredentialDto.builder()
                .credentialId(Base64.getEncoder().encodeToString(dummyWebAuthnId))
                .salt(Base64.getEncoder().encodeToString(dummyWebAuthnSalt))
                .transports("hybrid,internal".split(","))
                .wrappedUserKey(dummyWebAuthnCipher)
                .build();
        var settings = dummySettings;
        if (userOpt.isPresent()){
            var user = userOpt.get();
            switch (method){
                case PASSWORD -> {
                    var detailsOpt = userRepository.getPasswordBasedKdfDetails(user.getId());
                    if (detailsOpt.isPresent()){
                        var details = detailsOpt.get();
                        settings = new KdfSettings(keyDerivationFunction, details.getSalt(), details.getParameters());
                        passwordWrappedKey = CipherDto.builder()
                                .decryptionMethod(CipherDecryptionMethod.USER_PASSWORD)
                                .iv(details.getIv() != null ? Base64.getEncoder().encodeToString(details.getIv()) : null)
                                .cipher(Base64.getEncoder().encodeToString(details.getCipher()))
                                .build();
                    }
                }
                case WEBAUTHN -> {
                    var detailsOpt = userRepository.getWebAuthnBasedKdfDetails(user.getId());
                    if (detailsOpt.isPresent()){
                        var details = detailsOpt.get();
                        settings = new KdfSettings(keyDerivationFunction, details.getSalt(), details.getParameters());
                        var webAuthnCipher = CipherDto.builder()
                                .decryptionMethod(CipherDecryptionMethod.WEBAUTHN_KEY)
                                .iv(details.getIv() != null ? Base64.getEncoder().encodeToString(details.getIv()) : null)
                                .cipher(Base64.getEncoder().encodeToString(details.getCipher()))
                                .build();
                        webAuthnCredential = WebAuthnCredentialDto.builder()
                                .credentialId(Base64.getEncoder().encodeToString(details.getWebAuthnCredentialId()))
                                .salt(Base64.getEncoder().encodeToString(details.getWebAuthnSalt()))
                                .transports(details.getWebAuthnTransports().split(","))
                                .wrappedUserKey(webAuthnCipher)
                                .build();
                    }
                }
            }
        } else {
            switch (method){
                case PASSWORD -> {
                    var dummy = userRepository.getPasswordBasedKdfDetails(0);
                    ApplicationUtilities.deoptimize(dummy);
                }
                case WEBAUTHN -> {
                    var dummy = userRepository.getWebAuthnBasedKdfDetails(0);
                    ApplicationUtilities.deoptimize(dummy);
                }
            }
        }

        var detailsBuilder = KdfDetailsDto.builder()
                .algorithm(keyDerivationFunction.getIdentifier())
                .parameters(settings.getParameters())
                .salt(Base64.getEncoder().encodeToString(settings.getSalt()))
                .signatureVerificationWindow(UserService.SIGNATURE_VERIFICATION_WINDOW);
        switch (method){
            case PASSWORD -> detailsBuilder = detailsBuilder.wrappedUserKey(passwordWrappedKey);
            case WEBAUTHN -> detailsBuilder = detailsBuilder.webAuthnCredential(webAuthnCredential);
        }
        return detailsBuilder.build();
    }
}
