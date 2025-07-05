package net.cycastic.portfoliotoolkit.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.BaseJwtConfiguration;
import net.cycastic.portfoliotoolkit.configuration.auth.JwtConfiguration;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.dto.JwkDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.auth.AsymmetricJwtVerifier;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class StandardJwtService implements JwtIssuer, AsymmetricJwtVerifier {
    private static final Logger logger = LoggerFactory.getLogger(StandardJwtService.class);
    private final BaseJwtConfiguration jwtConfiguration;
    private final ECPrivateKey privateKey;
    private final ECPublicKey publicKey;

    public StandardJwtService(JwtConfiguration jwtConfiguration){
        this.jwtConfiguration = jwtConfiguration;
        privateKey = decodePrivateKey(jwtConfiguration.getPrivateKey());
        publicKey = decodePublicKey(jwtConfiguration.getPublicKey());
    }

    public StandardJwtService(BaseJwtConfiguration jwtConfiguration, ECPrivateKey privateKey, ECPublicKey publicKey){

        this.jwtConfiguration = jwtConfiguration;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public static @NonNull ECPrivateKey decodePrivateKey(@NotNull String base64Private) {
        try {
            var keyBytes = Base64.getDecoder().decode(base64Private);
            var keySpec = new PKCS8EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            return (ECPrivateKey)kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load EC private key", e);
        }
    }

    public static @NonNull ECPublicKey decodePublicKey(@NotNull String base64Public) {
        try {
            var keyBytes = Base64.getDecoder().decode(base64Public);
            var keySpec = new X509EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            return (ECPublicKey)kf.generatePublic(keySpec);
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

    @Override
    @SneakyThrows
    public JwkDto getJwk() {
        var fieldSize =  publicKey.getParams().getCurve().getField().getFieldSize();
        var crv = switch (fieldSize) {
            case 256 -> "P-256";
            case 384 -> "P-384";
            case 521 -> "P-521";
            default -> throw new IllegalArgumentException("Unsupported curve");
        };
        var alg = switch (fieldSize) {
            case 256 -> "ES256";
            case 384 -> "ES384";
            case 521 -> "ES521";
            default -> throw new IllegalArgumentException("Unsupported curve");
        };
        var urlEnc = Base64.getUrlEncoder().withoutPadding();
        var x = urlEnc.encodeToString(publicKey.getW().getAffineX().toByteArray());
        var y = urlEnc.encodeToString(publicKey.getW().getAffineY().toByteArray());
        var md = MessageDigest.getInstance("SHA-256");
        var digest = md.digest(publicKey.getEncoded());
        var kid = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        return JwkDto.builder()
                .kty("EC")
                .crv(crv)
                .x(x)
                .y(y)
                .use("sig")
                .alg(alg)
                .kid(kid)
                .build();
    }
}
