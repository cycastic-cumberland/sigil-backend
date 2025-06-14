package net.cycastic.portfoliotoolkit.service.impl;

import an.awesome.pipelinr.Pipelinr;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.auth.extract.claims.ExtractClaimsCommand;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.JwtUtilities;
import net.cycastic.portfoliotoolkit.domain.SessionStorage;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoggedUserAccessorImpl implements LoggedUserAccessor {
    private record ClaimsWrapper(@Null Claims claims){}
    private static final String CLAIMS_IDENTIFIER = "$__claims";
    private static final Logger logger = LoggerFactory.getLogger(LoggedUserAccessorImpl.class);

    private final SessionStorage sessionStorage;
    private final Pipelinr pipelinr;

    private @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    public Optional<Integer> tryGetProjectId() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.PROJECT_ID_HEADER);
        if (header == null){
            return Optional.empty();
        }

        return ApplicationUtilities.tryParseInt(header);
    }

    private Claims createClaimsFromRequest(){
        var request = getAttributes().getRequest();
        final var authHeader = request.getHeader("Authorization");
        if (authHeader == null || (!authHeader.startsWith("Bearer ") && !authHeader.startsWith("bearer "))){
            return null;
        }
        final var jwt = authHeader.substring("Bearer ".length());
        try {
            return pipelinr.send(new ExtractClaimsCommand(jwt));
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
    public Optional<Integer> tryGetUserId() {
        var claims = getClaims();
        if (claims == null){
            return Optional.empty();
        }

        var entry = claims.getSubject();
        if (entry == null){
            return Optional.empty();
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
}
