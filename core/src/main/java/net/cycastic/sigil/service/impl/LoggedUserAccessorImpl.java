package net.cycastic.sigil.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.JwtUtilities;
import net.cycastic.sigil.domain.SessionStorage;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LoggedUserAccessorImpl implements LoggedUserAccessor {
    private record ClaimsWrapper(@Null Claims claims){}
    private static final String CLAIMS_IDENTIFIER = "$__claims";
    private static final Logger logger = LoggerFactory.getLogger(LoggedUserAccessorImpl.class);

    private final SessionStorage sessionStorage;
    private final JwtVerifier jwtVerifier;

    private @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    public OptionalInt tryGetTenantId() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.TENANT_ID_HEADER);
        if (header == null){
            return OptionalInt.empty();
        }

        return ApplicationUtilities.tryParseInt(header);
    }

    @Override
    public OptionalInt tryGetPartitionId() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.PARTITION_ID_HEADER);
        if (header == null){
            return OptionalInt.empty();
        }

        return ApplicationUtilities.tryParseInt(header);
    }

    @Override
    public Optional<String> tryGetEncryptionKey() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.ENCRYPTION_KEY_HEADER);

        return Optional.ofNullable(header);
    }

    private Claims createClaimsFromRequest(){
        var request = getAttributes().getRequest();
        final var authHeader = request.getHeader("Authorization");
        if (authHeader == null || (!authHeader.startsWith("Bearer ") && !authHeader.startsWith("bearer "))){
            return null;
        }
        final var jwt = authHeader.substring("Bearer ".length());
        try {
            return jwtVerifier.extractClaims(jwt);
        } catch (RequestException e){
            logger.error("RequestException caught while extracting claims", e);
            return null;
        }
    }

    public @Null Claims getClaims(){
        var wrapper = sessionStorage.get(CLAIMS_IDENTIFIER, ClaimsWrapper.class);
        if (wrapper != null){
            return wrapper.claims;
        }

        var claims = createClaimsFromRequest();
        sessionStorage.put(CLAIMS_IDENTIFIER, new ClaimsWrapper(claims));
        return claims;
    }

    public boolean hasInvalidClaims(){
        var wrapper = sessionStorage.get(CLAIMS_IDENTIFIER, ClaimsWrapper.class);
        if (wrapper != null){
            return wrapper.claims == null;
        }
        var claims = createClaimsFromRequest();
        sessionStorage.put(CLAIMS_IDENTIFIER, new ClaimsWrapper(claims));
        return claims == null;
    }

    @Override
    public OptionalInt tryGetUserId() {
        var claims = getClaims();
        if (claims == null){
            return OptionalInt.empty();
        }

        var entry = claims.getSubject();
        if (entry == null){
            return OptionalInt.empty();
        }
        return ApplicationUtilities.tryParseInt(entry);
    }

    @Override
    public Set<String> getRoles() {
        var claims = getClaims();
        if (claims == null){
            return HashSet.newHashSet(0);
        }

        return JwtUtilities.extractRoles(claims);
    }

    @Override
    public String getRequestPath() {
        var attrs = getAttributes();
        var request = attrs.getRequest();

        var url = request.getRequestURL();
        var query = request.getQueryString();

        if (query != null) {
            url.append('?').append(query);
        }

        return url.toString();
    }

    @Override
    public Locale getRequestLocale() {
        return getAttributes().getRequest().getLocale();
    }
}
