package net.cycastic.sigil.application.auth;

import an.awesome.pipelinr.Command;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.Machine2MachineConfiguration;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.feign.m2m.M2MConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Component
public class AuthenticateM2MRequestMiddleware implements Command.Middleware {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticateM2MRequestMiddleware.class);
    private final PublicKey publicKey;
    private final boolean disabled;

    @SneakyThrows
    public AuthenticateM2MRequestMiddleware(Machine2MachineConfiguration machine2MachineConfiguration){
        var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(machine2MachineConfiguration.getPublicRsaKey()));
        var kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(keySpec);
        disabled = Objects.requireNonNullElse(machine2MachineConfiguration.getDisableAuthentication(), false);
    }

    private static @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    @SneakyThrows
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        if (disabled){
            return next.invoke();
        }

        var request = getAttributes().getRequest();

        var signatureHeader = request.getHeader(M2MConfigurations.SIGNATURE_HEADER);
        var signatureAlgHeader = request.getHeader(M2MConfigurations.SIGNATURE_ALGORITHM_HEADER);
        if (signatureHeader == null || signatureAlgHeader == null){
            logger.error("Unauthenticated request");
            throw RequestException.unauthorized();
        }
        if (!Objects.equals(signatureAlgHeader, "SHA256withRSA/PSS")){
            var e = new RequestException(400, "Unsupported algorithm: " + signatureAlgHeader);
            logger.error("Unsupported algorithm", e);
            throw e;
        }

        byte[] signature;
        try {
            signature = Base64.getDecoder().decode(signatureHeader);
        } catch (IllegalArgumentException e){
            logger.error("Malformed M2M signature", e);
            throw RequestException.unauthorized(e);
        }

        var digest = SlimCryptographicUtilities.digestSha256(request.getMethod().getBytes(StandardCharsets.UTF_8),
                request.getRequestURI().getBytes(StandardCharsets.UTF_8));

        var verifier = Signature.getInstance("RSASSA-PSS");
        verifier.setParameter(SlimCryptographicUtilities.getStandardPssSpec());
        verifier.initVerify(publicKey);
        verifier.update(digest);
        if (!verifier.verify(signature)){
            logger.error("Signature verification failed");
            throw RequestException.unauthorized();
        }

        return next.invoke();
    }
}
