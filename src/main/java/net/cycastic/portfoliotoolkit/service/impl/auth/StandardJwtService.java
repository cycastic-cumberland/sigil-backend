package net.cycastic.portfoliotoolkit.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import net.cycastic.portfoliotoolkit.configuration.auth.JwtConfiguration;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Lazy
@Service
public class StandardJwtService implements JwtIssuer, JwtVerifier {
    private final JwtConfiguration jwtConfiguration;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public StandardJwtService(JwtConfiguration jwtConfiguration){
        this.jwtConfiguration = jwtConfiguration;
        privateKey = getPrivateKey();
        publicKey = getPublicKey();
    }

    private @NonNull PrivateKey getPrivateKey() {
        try {
            var base64Private = jwtConfiguration.getPrivateKey();
            var keyBytes = Base64.getDecoder().decode(base64Private);
            var keySpec = new PKCS8EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load EC private key", e);
        }
    }

    private @NonNull PublicKey getPublicKey() {
        try {
            var base64Public = jwtConfiguration.getPublicKey();
            var keyBytes = Base64.getDecoder().decode(base64Public);
            var keySpec = new X509EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load EC public key", e);
        }
    }

    @Override
    public @NonNull String generateTokens(@NonNull String subject, Map<String, Object> extraClaims) {
        var now = new Date();
        var exp = new Date();
        exp.setTime(now.getTime() + jwtConfiguration.getValidForMillis());

        var authTokenBuilder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(privateKey);
        if (jwtConfiguration.getIssuer() != null){
            authTokenBuilder.setIssuer(jwtConfiguration.getIssuer());
        }
        if (extraClaims != null){
            authTokenBuilder.setClaims(extraClaims);
        }
        return authTokenBuilder.compact();
    }

    @Override
    public @NonNull String refreshToken(@NonNull String authToken) {
        var claims = extractClaimsInternal(authToken);
        var cutoff = new Date();
        cutoff.setTime(cutoff.getTime() + ApplicationConstants.REFRESH_TOKEN_TIME_MILLISECONDS);
        if (!extractClaim(claims, Claims::getExpiration).before(cutoff)){
            throw new RequestException(401, "Token is expired");
        }

        var allClaims = extractClaimsInternal(authToken);
        allClaims.remove(Claims.ISSUED_AT);
        allClaims.remove(Claims.SUBJECT);
        allClaims.remove(Claims.EXPIRATION);

        var subject = extractClaim(claims, Claims::getSubject);
        return generateTokens(subject, allClaims);
    }

    private Claims extractClaimsInternal(@NonNull String jwt){
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private <T> @NonNull T extractClaim(@NonNull Claims claims, @NonNull Function<Claims, T> claimsResolver){
        return claimsResolver.apply(claims);
    }

    @Override
    public @NonNull Map<String, Object> extractClaims(@NonNull String jwt) {
        return extractClaimsInternal(jwt);
    }
}
