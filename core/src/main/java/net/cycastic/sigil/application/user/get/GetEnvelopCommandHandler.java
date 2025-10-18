package net.cycastic.sigil.application.user.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.dto.auth.EnvelopDto;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GetEnvelopCommandHandler implements Command.Handler<GetEnvelopCommand, EnvelopDto> {
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public EnvelopDto handle(GetEnvelopCommand command) {
        var user = userRepository.getByEmail(command.getUserEmail())
                .orElseThrow(RequestException::forbidden);
        if (!loggedUserAccessor.isAdmin() && loggedUserAccessor.getUserId() != user.getId()){
            throw RequestException.forbidden();
        }
        var password = userRepository.getPasswordBasedKdfDetails(user.getId())
                .stream()
                .map(details -> CipherDto.builder()
                        .decryptionMethod(CipherDecryptionMethod.USER_PASSWORD)
                        .iv(details.getIv() != null ? Base64.getEncoder().encodeToString(details.getIv()) : null)
                        .cipher(Base64.getEncoder().encodeToString(details.getCipher()))
                        .build())
                .findFirst()
                .orElse(null);
        var webAuthn = userRepository.getWebAuthnBasedKdfDetails(user.getId())
                .stream()
                .map(details -> {
                    var webAuthnCipher = CipherDto.builder()
                            .decryptionMethod(CipherDecryptionMethod.WEBAUTHN_KEY)
                            .iv(details.getIv() != null ? Base64.getEncoder().encodeToString(details.getIv()) : null)
                            .cipher(Base64.getEncoder().encodeToString(details.getCipher()))
                            .build();
                    return WebAuthnCredentialDto.builder()
                            .credentialId(Base64.getEncoder().encodeToString(details.getWebAuthnCredentialId()))
                            .salt(Base64.getEncoder().encodeToString(details.getWebAuthnSalt()))
                            .transports(details.getWebAuthnTransports().split(","))
                            .wrappedUserKey(webAuthnCipher)
                            .build();
                })
                .findFirst()
                .orElse(null);
        return EnvelopDto.builder()
                .passwordCipher(password)
                .webAuthnCipher(webAuthn)
                .build();
    }
}
