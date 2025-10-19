package net.cycastic.sigil.configuration.feign;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.domain.exception.ApiRequestException;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.AsymmetricSignatureProvider;
import net.cycastic.sigil.service.feign.m2m.M2MConfigurations;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

@Configuration
public class FeignClientConfigurations {
    @SneakyThrows
    private static String getUri(String targetUrl, String relativePath){
        var targetPath = URI.create(targetUrl).getRawPath();
        return Path.of(targetPath, relativePath).toString();
    }

    @Bean
    public RequestInterceptor requestInterceptor(@Qualifier("m2MRSASignatureProvider") AsymmetricSignatureProvider asymmetricSignatureProvider){
        return template -> {
            var targetUrl = template.feignTarget().url();
            var method = template.method();
            var url = template.url();
            var urlParts = url.split("\\?");
            if (urlParts.length > 1){
                url = urlParts[0];
            }

            url = getUri(targetUrl, url);

            var digest = SlimCryptographicUtilities.digestSha256(method.getBytes(StandardCharsets.UTF_8),
                    url.getBytes(StandardCharsets.UTF_8));
            var signature = asymmetricSignatureProvider.sign(digest);
            var signatureAlgorithm = asymmetricSignatureProvider.getAlgorithm();
            template.header(M2MConfigurations.SIGNATURE_HEADER, Base64.getEncoder().encodeToString(signature));
            template.header(M2MConfigurations.SIGNATURE_ALGORITHM_HEADER, signatureAlgorithm);
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
