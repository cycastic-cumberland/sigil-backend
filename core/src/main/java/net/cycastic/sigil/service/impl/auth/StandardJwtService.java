package net.cycastic.sigil.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.auth.BaseJwtConfiguration;
import net.cycastic.sigil.configuration.auth.JwtConfiguration;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.auth.JwkDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.auth.AsymmetricJwtVerifier;
import net.cycastic.sigil.service.auth.JwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class StandardJwtService implements JwtIssuer, AsymmetricJwtVerifier {
    private static final Logger logger = LoggerFactory.getLogger(StandardJwtService.class);
    private final BaseJwtConfiguration jwtConfiguration;
    private final ECPrivateKey privateKey;
    private final ECPublicKey publicKey;
    private final String kid;

    public StandardJwtService(JwtConfiguration jwtConfiguration){
        this(jwtConfiguration,
                CryptographicUtilities.Keys.decodeECPrivateKey(Base64.getDecoder().decode(jwtConfiguration.getPrivateKey())),
                CryptographicUtilities.Keys.decodeECPublicKey(Base64.getDecoder().decode(jwtConfiguration.getPublicKey())));
    }

    public StandardJwtService(BaseJwtConfiguration jwtConfiguration, ECPrivateKey privateKey, ECPublicKey publicKey){
        this.jwtConfiguration = jwtConfiguration;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        var digest = CryptographicUtilities.digestSha256(publicKey.getEncoded());
        kid = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
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
                .setHeaderParam("kid", kid)
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
