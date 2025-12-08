package net.cycastic.sigil.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GravatarService {
    @Data
    public static class GravatarAvatar{
        private String base64;
    }

    @Component
    @RequiredArgsConstructor
    public static class GravatarServiceCore {
        @SneakyThrows
        @Cacheable(value = CacheConfigurations.Presets.LONG_LIVE_CACHE, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                key = "'GravatarServiceCore::getAvatar' + '?userEmail=' + #userEmail.toLowerCase() + '&size=' + #size")
        public GravatarAvatar getAvatar(String userEmail, int size){
            var emailLower = userEmail.toLowerCase();
            var emailHash = SlimCryptographicUtilities.digestSha256(emailLower.getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : emailHash) {
                hex.append(String.format("%02x", b));
            }
            var gravatarUrl = "https://www.gravatar.com/avatar/" + hex + "?d=identicon&s=" + size;

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(gravatarUrl))
                    .GET()
                    .build();

            try (var client = java.net.http.HttpClient.newHttpClient()){
                var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                if ((response.statusCode() < 200 && response.statusCode() > 299)){
                    throw new RequestException(503, "Failed to get user avatar");
                }
                if (!"image/png".equalsIgnoreCase(response.headers().firstValue("Content-Type").orElse(""))){
                    throw new RequestException(503, "User avatar is not a valid PNG");
                }

                var avatar = new GravatarAvatar();
                avatar.setBase64(Base64.getEncoder().encodeToString(response.body()));
                return avatar;
            }
        }
    }

    private final GravatarServiceCore gravatarServiceCore;

    public byte[] getAvatar(String userEmail, int size){
        if (size < 100){
            throw new RequestException(400, "Invalid size");
        }

        var image = gravatarServiceCore.getAvatar(userEmail, size);
        return Base64.getDecoder().decode(image.getBase64());
    }
}
