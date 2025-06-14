package net.cycastic.portfoliotoolkit.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import net.cycastic.portfoliotoolkit.configuration.BaseJwtConfiguration;
import net.cycastic.portfoliotoolkit.configuration.auth.JwtConfiguration;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class StandardJwtService implements JwtIssuer, JwtVerifier {
    private static final Logger logger = LoggerFactory.getLogger(StandardJwtService.class);
    private final BaseJwtConfiguration jwtConfiguration;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public StandardJwtService(JwtConfiguration jwtConfiguration){
        this.jwtConfiguration = jwtConfiguration;
        privateKey = decodePrivateKey(jwtConfiguration.getPrivateKey());
        publicKey = decodePublicKey(jwtConfiguration.getPublicKey());
    }

    public StandardJwtService(BaseJwtConfiguration jwtConfiguration, PrivateKey privateKey, PublicKey publicKey){

        this.jwtConfiguration = jwtConfiguration;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public static @NonNull PrivateKey decodePrivateKey(@NotNull String base64Private) {
        try {
            var keyBytes = Base64.getDecoder().decode(base64Private);
            var keySpec = new PKCS8EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load EC private key", e);
        }
    }

    public static @NonNull PublicKey decodePublicKey(@NotNull String base64Public) {
        try {
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

        var authTokenBuilder = Jwts.builder();
        if (extraClaims != null && !extraClaims.isEmpty()){
            authTokenBuilder.setClaims(extraClaims);
        }
        authTokenBuilder = authTokenBuilder
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(privateKey);
        if (jwtConfiguration.getIssuer() != null){
            authTokenBuilder.setIssuer(jwtConfiguration.getIssuer());
        }
        return authTokenBuilder.compact();
    }

    @Override
    public @NonNull String refreshToken(@NonNull String authToken) {
        var claims = extractExpiredClaims(authToken);
        var subject = claims.getSubject();
        var cutoff = new Date();
        cutoff.setTime(cutoff.getTime() + ApplicationConstants.REFRESH_TOKEN_TIME_MILLISECONDS);
        if (!claims.getExpiration().before(cutoff)){
            throw new RequestException(401, "Token is expired");
        }

        claims.remove(Claims.ISSUED_AT);
        claims.remove(Claims.SUBJECT);
        claims.remove(Claims.EXPIRATION);

        return generateTokens(subject, claims);
    }

    private @NonNull Claims extractExpiredClaims(@NonNull String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (ExpiredJwtException e){
            return e.getClaims();
        } catch (Exception e){
            logger.error("Exception caught while parsing token", e);
            throw new RequestException(401, "Failed to authenticate request");
        }
    }

    @Override
    public @NonNull Claims extractClaims(@NonNull String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e){
            logger.error("Exception caught while parsing token", e);
            throw new RequestException(401, "Failed to authenticate request");
        }
    }
}
