package net.cycastic.sigil.application.auth.webauthn.enroll;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.WebAuthnCredential;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.WebAuthnCredentialRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class EnrollWebAuthnEnvelopCommandHandler implements Command.Handler<EnrollWebAuthnEnvelopCommand, @Null Object> {
    private final WebAuthnCredentialRepository webAuthnCredentialRepository;
    private final CipherRepository cipherRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Object handle(EnrollWebAuthnEnvelopCommand command) {
        var user = userService.getUser();
        if (webAuthnCredentialRepository.existsByUser(user)){
            throw RequestException.withExceptionCode("C409T000");
        }
        var transports = Arrays.stream(command.getTransports())
                .map(String::trim)
                .map(String::toLowerCase)
                .sorted(String::compareTo)
                .toList();
        if (transports.stream().anyMatch(t -> t.contains(","))){
            throw new RequestException(400, "Invalid transport");
        }

        var cipher = new Cipher(CipherDecryptionMethod.WEBAUTHN_KEY,
                Base64.getDecoder().decode(command.getWrappedUserKey().getIv()),
                Base64.getDecoder().decode(command.getWrappedUserKey().getCipher()));
        cipherRepository.save(cipher);

        var webAuthnCredential = WebAuthnCredential.builder()
                .user(user)
                .credentialId(Base64.getDecoder().decode(command.getCredentialId()))
                .salt(Base64.getDecoder().decode(command.getSalt()))
                .transports(String.join(",", transports))
                .wrappedUserKey(cipher)
                .build();
        webAuthnCredentialRepository.save(webAuthnCredential);
        return null;
    }
}
