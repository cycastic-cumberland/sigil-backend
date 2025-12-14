package net.cycastic.sigil.configuration.feign;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.domain.exception.ApiRequestException;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.AsymmetricSignatureProvider;
import net.cycastic.sigil.service.m2m.M2MConfigurations;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class FeignClientConfigurations {
    private static final String CALLER = "sigil-core";

    @Bean
    public RequestInterceptor requestInterceptor(@Qualifier("m2MRSASignatureProvider") AsymmetricSignatureProvider asymmetricSignatureProvider){
        return template -> {
            var nonce = UUID.randomUUID().toString();
            var digest = SlimCryptographicUtilities.digestSha256(CALLER.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8));
            var signature = asymmetricSignatureProvider.sign(digest);
            var signatureAlgorithm = asymmetricSignatureProvider.getAlgorithm();
            template.header(M2MConfigurations.SIGNATURE_HEADER, Base64.getEncoder().encodeToString(signature));
            template.header(M2MConfigurations.SIGNATURE_ALGORITHM_HEADER, signatureAlgorithm);
            template.header(M2MConfigurations.REQUEST_CALLER_HEADER, CALLER);
            template.header(M2MConfigurations.REQUEST_NONCE_HEADER, nonce);
        };
    }

    @Bean
    public ErrorDecoder errorDecoder(JsonSerializer jsonSerializer){
        return (methodKey, response) -> {
            ExceptionResponse exception;
            try (var is = response.body().asInputStream()){
                exception = jsonSerializer.deserialize(is, ExceptionResponse.class);
            } catch (Exception e){
                return new RequestException(500, e, "Internal server error");
            }
            if (exception.getStatus() <= 0){
                return new RequestException(500, "Internal server error");
            }

            return new ApiRequestException(exception);
        };
    }
}
