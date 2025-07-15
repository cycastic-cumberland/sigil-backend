package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.configuration.auth.KdfConfiguration;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class GetKdfSettingsCommandHandler implements Command.Handler<GetKdfSettingsCommand, KdfDetailsDto> {
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
                if (salt.length != KeyDerivationFunction.SALT_SIZE) {
                    salt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, kdfConfiguration.getMaskingKey().getBytes(StandardCharsets.UTF_8), null);
                }
            } catch (IllegalArgumentException e){
                salt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, kdfConfiguration.getMaskingKey().getBytes(StandardCharsets.UTF_8), null);
            }
        } else {
            salt = new byte[KeyDerivationFunction.SALT_SIZE];
        }

        maskingKey = salt;
        maskingPrivateKey = Base64.getDecoder().decode(kdfConfiguration.getMaskingRsaPrivateKey());
    }

    @Override
    @SneakyThrows
    public KdfDetailsDto handle(GetKdfSettingsCommand command) {
        final var dummySalt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, command.getUserEmail().getBytes(StandardCharsets.UTF_8), maskingKey);
        final var dummyEncryptionKey = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, command.getUserEmail().getBytes(StandardCharsets.UTF_8), maskingKey);
        final var dummyEncryptionNonce = CryptographicUtilities.deriveKey(CryptographicUtilities.NONCE_LENGTH, command.getUserEmail().getBytes(StandardCharsets.UTF_8), maskingKey);
        final var dummyKey = new SecretKeySpec(dummyEncryptionKey, "AES");
        final var dummyEncryptedPrivateKey = CryptographicUtilities.encrypt(dummyKey, dummyEncryptionNonce, maskingPrivateKey);
        final var dummyKeyKid = CryptographicUtilities.digestSha256(dummyEncryptionKey);

        var userOpt = userRepository.getByEmail(command.getUserEmail());
        CipherDto wrappedUserKey;
        KeyDerivationFunction.KeyDerivationSettings settings;
        if (userOpt.isPresent()){
            final var user = userOpt.get();
            final KeyDerivationFunction.Parameters parameters;
            try (var stream = new ByteArrayInputStream(user.getKdfSettings())){
                parameters = keyDerivationFunction.getParameters(stream);
            }

            settings = new KeyDerivationFunction.KeyDerivationSettings(){
                @Override
                public byte[] getSalt() {
                    return user.getKdfSalt();
                }

                @Override
                public KeyDerivationFunction.Parameters getParameters() {
                    return parameters;
                }
            };
            wrappedUserKey = CipherDto.fromDomain(user.getWrappedUserKey());
        } else {
            settings = new KeyDerivationFunction.KeyDerivationSettings() {
                @Override
                public byte[] getSalt() {
                    return dummySalt;
                }

                @Override
                public KeyDerivationFunction.Parameters getParameters() {
                    return defaultParameters;
                }
            };

            wrappedUserKey = CipherDto.builder()
                    .decryptionMethod(CipherDecryptionMethod.USER_PASSWORD)
                    .kid(Base64.getEncoder().encodeToString(dummyKeyKid))
                    .iv(Base64.getEncoder().encodeToString(dummyEncryptionNonce))
                    .cipher(Base64.getEncoder().encodeToString(dummyEncryptedPrivateKey))
                    .build();
        }

        return KdfDetailsDto.builder()
                .algorithm(keyDerivationFunction.getIdentifier())
                .parameters(settings.getParameters())
                .salt(Base64.getEncoder().encodeToString(settings.getSalt()))
                .wrappedUserKey(wrappedUserKey)
                .signatureVerificationWindow(UserService.SIGNATURE_VERIFICATION_WINDOW)
                .build();
    }
}
